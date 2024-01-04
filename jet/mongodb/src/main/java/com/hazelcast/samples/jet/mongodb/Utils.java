/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.samples.jet.mongodb;

import com.hazelcast.examples.helper.CommonUtils;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Simple utility methods to be used in tests.
 */
final class Utils {

    private static final String[] FAKE_CARD_NUMBERS = generateNumbers();
    private static final String[] CITIES = new String[] {
            "Wrocław", "Warszawa", "London", "Białystok", "Brno", "Praga", "Ankara", "Instambuł", "Kyyiv"
    };
    private static final Random RANDOM = new Random();

    private Utils() {
    }

    /**
     * Initializes database for the examples.
     */
    static void initMongoDatabase(String connectionString) {
        try (MongoClient client = MongoClients.create(connectionString)) {
            var options = new CreateCollectionOptions();
            var validationOptions = new ValidationOptions();
            validationOptions.validator(BsonDocument.parse(
                    """
                            {
                                $jsonSchema: {
                                  bsonType: "object",
                                  title: "Payment Object Validation",
                                  properties: {
                                    "paymentId": { "bsonType": "objectId" }
                                    "cardNo": { "bsonType": "string" }
                                    "city": { "bsonType": "string" }
                                    "amount": { "bsonType": "decimal" }
                                    "successful": { "bsonType": "bool" }
                                  }
                                }
                              }
                            """
            ));
            options.validationOptions(validationOptions);
            var database = client.getDatabase("shop");
            database.createCollection("payments", options);
        }
    }

    /**
     * Starts a thread that puts the data into Mongo.
     */
    static void startDataIngestionThread(String connectionString) {
        new Thread(() -> {
            try (var client = MongoClients.create(connectionString)) {
                var database = client.getDatabase("shop");
                MongoCollection<Document> collection = database.getCollection("payments");
                while (!Thread.interrupted()) {
                    collection.insertOne(new Document("cardNo", fakeCardNo())
                            .append("city", randomCity())
                            .append("amount", new BigDecimal(RANDOM.nextInt(10000)))
                            .append("successful", RANDOM.nextInt(100) % 4 > 0)
                            .append("paymentId", ObjectId.get())
                    );
                    CommonUtils.sleepMillis(100 + RANDOM.nextInt(500));
                }

            }
        }).start();
    }

    private static String fakeCardNo() {
        return FAKE_CARD_NUMBERS[RANDOM.nextInt(FAKE_CARD_NUMBERS.length)];
    }

    private static String randomCity() {
        return CITIES[RANDOM.nextInt(CITIES.length)];
    }

    private static String[] generateNumbers() {
        String[] numbers = new String[1000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = RandomStringUtils.random(20, false, true);
        }
        return numbers;
    }
}
