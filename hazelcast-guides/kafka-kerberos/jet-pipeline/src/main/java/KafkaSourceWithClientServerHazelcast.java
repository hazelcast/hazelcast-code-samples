/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

package com.example.hazelcast.jet.kafka;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import static com.hazelcast.jet.kafka.KafkaSources.kafka;
import com.hazelcast.jet.pipeline.Sinks;
import static com.hazelcast.jet.pipeline.Sinks.map;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.map.IMap;

import java.util.Properties;
import java.util.AbstractMap.SimpleEntry;

import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;


public class KafkaSourceWithClientServerHazelcast {

    private static final ILogger LOGGER = Logger.getLogger(KafkaSourceWithClientServerHazelcast.class);
    private static final String AUTO_OFFSET_RESET = "earliest";

    private static final String SINK_NAME = "sink_orders";
    private static final String INTERNAL_DOCKER_BROKER_ADDRESS = "broker.kerberos.example:9092";

    private static Pipeline buildPipeline() {
        Properties props = new Properties();
        // Tell Hazelcast the value format of the Kafka records
        props.setProperty("value.format", "json-flat");
        // KAFKA CONSUMER CONFIGURATION
        // Use an internal broker address because the pipeline will be executed in the Hazelcast Docker container
        props.setProperty("bootstrap.servers", INTERNAL_DOCKER_BROKER_ADDRESS);
        props.setProperty("key.deserializer", IntegerDeserializer.class.getCanonicalName());
        props.setProperty("value.deserializer", StringDeserializer.class.getCanonicalName());
        props.setProperty("sasl.mechanism", "GSSAPI");
        // Provides authentication, not encryption
        props.setProperty("security.protocol", "SASL_PLAINTEXT");
        props.setProperty("sasl.kerberos.service.name", "kafka");
        props.setProperty("sasl.jaas.config", "com.sun.security.auth.module.Krb5LoginModule required useTicketCache=true useKeyTab=true storeKey=true keyTab='/mnt/jduke.keytab' principal='jduke@KERBEROS.EXAMPLE';");
        props.setProperty("auto.offset.reset", AUTO_OFFSET_RESET);
        Pipeline p = Pipeline.create();
        p.readFrom(kafka(props, "orders"))
                .withoutTimestamps()
                // Use the ID in the JSON value as the map's key
                .map(entry -> new SimpleEntry<>(JsonUtil.mapFrom(entry.getValue()).get("id"), entry.getValue()))
                .writeTo(map(SINK_NAME));
        System.out.println("Orders added to map");
        System.out.println("===================");
        return p;
    }
    
    public static void main(String[] args) {
        new KafkaSourceWithClientServerHazelcast().run();
    }

    private void run() {
      HazelcastInstance hz = Hazelcast.bootstrappedInstance();
      JetService jet = hz.getJet();

      Pipeline p = buildPipeline();

      Job job = jet.newJob(p);
    }
}