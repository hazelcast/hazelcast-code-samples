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
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.mongodb.MongoSources;
import com.hazelcast.jet.mongodb.ResourceChecks;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.WindowDefinition;
import com.mongodb.client.MongoClients;
import org.bson.BsonTimestamp;
import org.bson.types.ObjectId;
import org.testcontainers.containers.MongoDBContainer;

import java.math.BigDecimal;

import static com.hazelcast.function.ComparatorEx.comparing;
import static com.hazelcast.jet.aggregate.AggregateOperations.topN;
import static com.hazelcast.samples.jet.mongodb.MongoSourceExample.Payment.FORMAT_STRING;
import static com.hazelcast.samples.jet.mongodb.MongoSourceExample.Payment.FORMAT_STRING_HEADER;
import static com.mongodb.client.model.Filters.eq;
import static java.lang.String.format;

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
public class MongoSourceExample {

    public static void main(String[] args) {
        if (args.length > 0) {
            new MongoSourceExample().run(args[0]);
        } else {
            try (var mongoContainer = new MongoDBContainer("mongo:latest")) {
                mongoContainer.start();
                new MongoSourceExample().run(mongoContainer.getConnectionString());
            }
        }
    }

    public record Payment (ObjectId paymentId, String cardNo, String city, BigDecimal amount, boolean successful) {

        public static final String FORMAT_STRING = "%-3s %-30s %-20s %-12s %-8.2f %-8s";
        public static final String FORMAT_STRING_HEADER = "%-3s %-30s %-20s %-12s %-8s %-8s";

        String asRow(int index) {
            return format(FORMAT_STRING, index, paymentId.toString(), cardNo, city, amount, successful);
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

        Utils.initMongoDatabase(connectionString);

        Utils.startDataIngestionThread(connectionString);

        Pipeline pipeline = Pipeline.create();
        StreamSource<Payment> streamSource = MongoSources
                .stream(() -> MongoClients.create(connectionString))
                .database("shop")
                .filter(eq("fullDocument.successful", true))
                .collection("payments", Payment.class)
                .checkResourceExistence(ResourceChecks.NEVER)
                .startAtOperationTime(new BsonTimestamp())
                .build();
        pipeline.readFrom(streamSource)
                .withIngestionTimestamps()
                .window(WindowDefinition.tumbling(2000))
                .aggregate(topN(5, comparing(Payment::amount)))

                .writeTo(Sinks.logger(windowResult -> {
                    var sb = new StringBuilder();
                    sb.append("\nTop payments in last two seconds\n");
                    sb.append(format(FORMAT_STRING_HEADER, "no", "paymentId", "cardNo", "city", "amount", "successful"))
                      .append("\n");
                    int index = 1;
                    for (var payment : windowResult.result()) {
                        sb.append(payment.asRow(index++)).append("\n");
                    }
                    return sb.toString();
                }));

        Job job = jet.newJob(pipeline);
        job.join();
    }
}
