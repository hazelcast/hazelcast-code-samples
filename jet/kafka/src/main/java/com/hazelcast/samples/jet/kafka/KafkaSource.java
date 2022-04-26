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
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.zk.EmbeddedZookeeper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A sample which consumes two Kafka topics and writes
 * the received items to an {@code IMap}.
 **/
public class KafkaSource {

    private static final ILogger LOGGER = Logger.getLogger(KafkaSource.class);
    private static final int MESSAGE_COUNT_PER_TOPIC = 1_000_000;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final boolean USE_EMBEDDED_KAFKA = Boolean.parseBoolean(System.getProperty("use.embedded.kafka", "true"));
    private static final String AUTO_OFFSET_RESET = "earliest";

    private static final String SINK_NAME = "sink";

    private EmbeddedZookeeper zkServer;
    private KafkaServer kafkaServer;
    private TopicUtil topicUtil;

    private static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();
        p.readFrom(KafkaSources.kafka(props(
                                "bootstrap.servers", BOOTSTRAP_SERVERS,
                                "key.deserializer", StringDeserializer.class.getCanonicalName(),
                                "value.deserializer", StringDeserializer.class.getCanonicalName(),
                                "auto.offset.reset", AUTO_OFFSET_RESET)
                , "t1", "t2"))
         .withoutTimestamps()
         .writeTo(Sinks.map(SINK_NAME));
        return p;
    }

    public static void main(String[] args) throws Exception {
        new KafkaSource().run();
    }

    private void run() throws Exception {
        try {
            if (USE_EMBEDDED_KAFKA) {
                createKafkaCluster();
            }
            topicUtil = new TopicUtil(BOOTSTRAP_SERVERS);

            fillTopics();

            HazelcastInstance hz = Hazelcast.bootstrappedInstance();
            JetService jet = hz.getJet();
            IMap<String, String> sinkMap = hz.getMap(SINK_NAME);

            Pipeline p = buildPipeline();

            long start = System.nanoTime();
            Job job = jet.newJob(p);
            while (true) {
                int mapSize = sinkMap.size();
                System.out.format("Received %d entries in %d milliseconds.%n",
                        mapSize, NANOSECONDS.toMillis(System.nanoTime() - start));
                if (mapSize == MESSAGE_COUNT_PER_TOPIC * 2) {
                    job.cancel();
                    break;
                }
                Thread.sleep(100);
            }
        } finally {
            Hazelcast.shutdownAll();
            topicUtil.deleteTopic("t1");
            topicUtil.deleteTopic("t2");
            topicUtil.close();
            if (USE_EMBEDDED_KAFKA) {
                shutdownKafkaCluster();
            }
        }
    }

    // Creates an embedded zookeeper server and a kafka broker
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

    // Creates 2 topics (t1, t2) with different partition counts (32, 64) and fills them with items
    private void fillTopics() {
        topicUtil.createTopic("t1", 32);
        topicUtil.createTopic("t2", 64);

        LOGGER.info("Filling Topics");
        Properties props = props(
                "bootstrap.servers", "localhost:9092",
                "key.serializer", StringSerializer.class.getName(),
                "value.serializer", StringSerializer.class.getName());
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= MESSAGE_COUNT_PER_TOPIC; i++) {
                producer.send(new ProducerRecord<>("t1", "t1-" + i, String.valueOf(i)));
                producer.send(new ProducerRecord<>("t2", "t2-" + i, String.valueOf(i)));
            }
            LOGGER.info("Published " + MESSAGE_COUNT_PER_TOPIC + " messages to topic t1");
            LOGGER.info("Published " + MESSAGE_COUNT_PER_TOPIC + " messages to topic t2");
        }
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
