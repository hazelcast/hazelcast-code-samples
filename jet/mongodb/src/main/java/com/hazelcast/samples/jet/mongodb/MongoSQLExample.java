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
import com.hazelcast.sql.SqlColumnMetadata;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRowMetadata;
import com.hazelcast.sql.SqlService;
import org.testcontainers.containers.MongoDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

/**
 * Simple example that continuously reads from Mongo using SQL,
 * prints average amount of payments for each city each 2 seconds.
 *
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
public class MongoSQLExample {

    public static void main(String[] args) {
        if (args.length > 0) {
            new MongoSQLExample().run(args[0]);
        } else {
            try (MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest")) {
                mongoContainer.start();
                new MongoSQLExample().run(mongoContainer.getConnectionString());
            }
        }
    }


    private void run(String connectionString) {
        System.out.println("Will use: " + connectionString);
        var config = new Config();
        config.setProperty("hazelcast.logging.type", "log4j2");
        config.getJetConfig().setEnabled(true);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        Utils.initMongoDatabase(connectionString);

        Utils.startDataIngestionThread(connectionString);

        CommonUtils.sleepSeconds(2);

        SqlService sql = hz.getSql();

        long startAt = System.currentTimeMillis();
        sql.execute("""
                create mapping payments external name shop.payments (
                    paymentId varchar external name "fullDocument.paymentId",
                    cardNo varchar external name "fullDocument.cardNo",
                    city varchar external name "fullDocument.city",
                    amount decimal external name "fullDocument.amount",
                    successful boolean external name "fullDocument.successful",
                    wallTime timestamp
                )
                connector type MongoStream
                options (
                    'connectionString' = '%s',
                    'startAt' = '%s'
                )
                """.formatted(connectionString, startAt)).close();

        try (SqlResult res = sql.execute("""
                 select window_end, city, avg(amount) as avgAmount
                 from table(tumble((
                    select * from table(impose_order(table payments, descriptor(wallTime), interval '2' second))
                    where successful = true
                 ), descriptor(wallTime), interval '2' second))
                 group by window_end, city
                """)) {
            print(res);
        }
    }

    static void print(SqlResult r) {
        AtomicInteger counter = new AtomicInteger(0);
        SqlRowMetadata rowMetadata = r.getRowMetadata();
        int colCount = rowMetadata.getColumnCount();
        List<String> cols = rowMetadata.getColumns().stream()
                                       .map(SqlColumnMetadata::getName)
                                       .collect(toCollection(ArrayList::new));
        cols.add(0, "no");
        String pattern = "%-3s | " + IntStream.range(0, colCount).mapToObj(i -> "%-26s | ").collect(joining()) + "%n";
        System.out.printf(pattern, cols.toArray());

        r.iterator().forEachRemaining(row -> {
            var values = IntStream.range(0, colCount)
                                  .mapToObj(row::getObject)
                                  .collect(toCollection(ArrayList::new));
            values.add(0, counter.getAndIncrement());
            System.out.printf(pattern, values.toArray());
        });
    }

}
