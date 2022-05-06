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

package com.hazelcast.samples.jet.kafka;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.zk.EmbeddedZookeeper;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A sample which consumes an {@code IMap} and writes
 * the received items to a Kafka Topic.
 **/
public class KafkaSink {

    private static final ILogger LOGGER = Logger.getLogger(KafkaSink.class);
    private static final int MESSAGE_COUNT = 50_000;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String SOURCE_NAME = "source";
    private static final String SINK_TOPIC_NAME = "t1";
    private static final boolean USE_EMBEDDED_KAFKA = Boolean.parseBoolean(System.getProperty("use.embedded.kafka", "true"));

    private EmbeddedZookeeper zkServer;
    private KafkaServer kafkaServer;
    private KafkaConsumer<String, String> kafkaConsumer;

    private static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();
        p.readFrom(Sources.map(SOURCE_NAME))
         .writeTo(KafkaSinks.kafka(props(
                         "bootstrap.servers", BOOTSTRAP_SERVERS,
                         "key.serializer", StringSerializer.class.getCanonicalName(),
                         "value.serializer", StringSerializer.class.getCanonicalName()),
                 SINK_TOPIC_NAME));
        return p;
    }

    public static void main(String[] args) throws Exception {
        new KafkaSink().run();
    }

    private void run() throws Exception {
        try {
            if (USE_EMBEDDED_KAFKA) {
                createKafkaCluster();
            }

            HazelcastInstance hz = Hazelcast.bootstrappedInstance();
            JetService jet = hz.getJet();

            IMap<String, String> sourceMap = hz.getMap(SOURCE_NAME);
            fillIMap(sourceMap);


            Pipeline p = buildPipeline();

            long start = System.nanoTime();
            Job job = jet.newJob(p);

            LOGGER.info("Consuming Topics");

            kafkaConsumer = createConsumer(SINK_TOPIC_NAME);

            int totalMessagesSeen = 0;
            while (true) {
                ConsumerRecords records = kafkaConsumer.poll(10000);
                totalMessagesSeen += records.count();
                System.out.format("Received %d entries in %d milliseconds.%n",
                        totalMessagesSeen, NANOSECONDS.toMillis(System.nanoTime() - start));
                if (totalMessagesSeen == MESSAGE_COUNT) {
                    job.cancel();
                    break;
                }
                Thread.sleep(100);
            }
        } finally {
            Hazelcast.shutdownAll();
            kafkaConsumer.close();
            if (USE_EMBEDDED_KAFKA) {
                shutdownKafkaCluster();
            }
        }
    }

    public KafkaConsumer<String, String> createConsumer(String... topicIds) {
        return createConsumer(StringDeserializer.class, StringDeserializer.class, emptyMap(), topicIds);
    }

    public <K, V> KafkaConsumer<K, V> createConsumer(
            Class<? extends Deserializer<K>> keyDeserializerClass,
            Class<? extends Deserializer<V>> valueDeserializerClass,
            Map<String, String> properties,
            String... topicIds
    ) {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        consumerProps.setProperty("group.id", "verification-consumer");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.setProperty("key.deserializer", keyDeserializerClass.getCanonicalName());
        consumerProps.setProperty("value.deserializer", valueDeserializerClass.getCanonicalName());
        consumerProps.setProperty("isolation.level", "read_committed");
        // to make sure the consumer starts from the beginning of the topic
        consumerProps.setProperty("auto.offset.reset", "earliest");
        consumerProps.putAll(properties);
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(topicIds));
        return consumer;
    }

    private void createKafkaCluster() throws IOException {
        LOGGER.info("Creating an embedded zookeeper server and a kafka broker");
        zkServer = new EmbeddedZookeeper();
        String zkConnect = "localhost:" + zkServer.port();

        KafkaConfig config = new KafkaConfig(props(
                "zookeeper.connect", zkConnect,
                "broker.id", "0",
                "log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString(),
                "offsets.topic.replication.factor", "1",
                "listeners", "PLAINTEXT://localhost:9092"));
        Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);
    }

    private void fillIMap(IMap<String, String> sourceMap) {
        LOGGER.info("Filling IMap");
        for (int i = 1; i <= MESSAGE_COUNT; i++) {
            sourceMap.put("t1-" + i, String.valueOf(i));
        }
        LOGGER.info("Published " + MESSAGE_COUNT + " messages to IMap -> " + SOURCE_NAME);
    }

    private void shutdownKafkaCluster() {
        kafkaServer.shutdown();
        zkServer.shutdown();
    }

    private static Properties props(String... kvs) {
        final Properties props = new Properties();
        for (int i = 0; i < kvs.length; ) {
            props.setProperty(kvs[i++], kvs[i++]);
        }
        return props;
    }
}
