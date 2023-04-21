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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.helper.CommonUtils;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.mongodb.MongoSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.WindowDefinition;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import org.bson.BsonDocument;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.Random;

import static com.hazelcast.function.ComparatorEx.comparing;
import static com.hazelcast.jet.aggregate.AggregateOperations.topN;
import static com.hazelcast.samples.jet.mongodb.MongoSource.Payment.FORMAT_STRING;

/**
 * Simple example that continuously reads from Mongo, picks top 5 payments in last 2 seconds and prints the results into the console.
 * <p>
 * You can run this example using one of two modes:
 * <ol>
 *      <li> Before running this example you can start a MongoDB instance, e.g. using Docker. Remember to configure
 *          replica sets, as streaming queries replicas! In this mode you should manually compose the {@code connectionString}
 *          using your Mongo instance coordinates and pass it as first argument of this example.
 *      </li>
 *      <li>
 *          If you don't provide any arguments, the example will start a TestContainer with Mongo and will automatically
 *          pick it's connection string.
 *      </li>
 * </ol>
 *
 */
public class MongoSource {
    private static final String[] FAKE_CARD_NUMBERS = generateNumbers();
    private static final String[] CITIES = new String[] {
            "Wrocław", "Warszawa", "London", "Białystok", "Brno", "Praga", "Ankara", "Instambuł", "Kyyiv"
    };
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        if (args.length > 0) {
            new MongoSource().run(args[0]);
        } else {
            try (MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest")) {
                mongoContainer.start();
                new MongoSource().run(mongoContainer.getConnectionString());
            }
        }
    }

    public record Payment (ObjectId paymentId, String cardNo, String city, BigDecimal amount, boolean successful) {

        public static final String FORMAT_STRING = "%-3s %-30s %-20s %-12s %-8s %-8s";

        String asRow(int index) {
            return String.format(FORMAT_STRING, index, paymentId.toString(), cardNo, city, amount, successful);
        }
    }

    private void run(String connectionString) {
        System.out.println("Will use: " + connectionString);
        var config = new Config();
        config.setProperty("hazelcast.logging.type", "log4j2");
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig()
              .getCompactSerializationConfig()
              .addClass(Payment.class);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();

        initMongoDatabase(connectionString);

        new Thread(() -> {
            try (MongoClient client = MongoClients.create(connectionString)) {
                MongoDatabase database = client.getDatabase("shop");
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

        Pipeline pipeline = Pipeline.create();
        StreamSource<Payment> streamSource = MongoSources
                .stream(() -> MongoClients.create(connectionString))
                .database("shop")
                .filter(Filters.eq("fullDocument.successful", true))
                .collection("payments", Payment.class)
                .throwOnNonExisting(false)
                .startAtOperationTime(new BsonTimestamp())
                .build();
        pipeline.readFrom(streamSource)
                .withIngestionTimestamps()

                .window(WindowDefinition.tumbling(2000))
                .aggregate(topN(5, comparing(Payment::amount)))

                .writeTo(Sinks.logger(windowResult -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nTop payments in last two seconds\n");
                    sb.append(String.format(FORMAT_STRING, "no", "paymentId", "cardNo", "city", "amount", "successful"))
                      .append("\n");
                    int index = 1;
                    for (Payment payment : windowResult.result()) {
                        sb.append(payment.asRow(index++)).append("\n");
                    }
                    return sb.toString();
                }));

        Job job = jet.newJob(pipeline);
        job.join();
    }

    private void initMongoDatabase(String connectionString) {
        try (MongoClient client = MongoClients.create(connectionString)) {
            CreateCollectionOptions options = new CreateCollectionOptions();
            ValidationOptions validationOptions = new ValidationOptions();
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
            MongoDatabase database = client.getDatabase("shop");
            database.createCollection("payments", options);
        }
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
