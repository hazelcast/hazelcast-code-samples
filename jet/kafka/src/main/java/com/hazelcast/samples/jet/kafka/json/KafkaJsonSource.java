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

package com.hazelcast.samples.jet.kafka.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.core.JobStatus;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;
import com.hazelcast.samples.jet.kafka.TopicUtil;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.zk.EmbeddedZookeeper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Properties;

import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.impl.util.Util.uncheckRun;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A sample which demonstrates how to consume items using custom JSON
 * serialization.
 */
public class KafkaJsonSource {

    private static final ILogger LOGGER = Logger.getLogger(KafkaJsonSource.class);
    private static final String ZK_HOST = "127.0.0.1";
    private static final String BROKER_HOST = "127.0.0.1";
    private static final String AUTO_OFFSET_RESET = "earliest";
    private static final String TOPIC = "topic";
    private static final String SINK_MAP_NAME = "users";
    private static final boolean USE_EMBEDDED_KAFKA = Boolean.parseBoolean(System.getProperty("use.embedded.kafka", "true"));

    private String bootstrapServers = "localhost:9092";
    private EmbeddedZookeeper zkServer;
    private KafkaServer kafkaServer;
    private TopicUtil topicUtil;

    private Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();
        p.readFrom(KafkaSources.<Integer, JsonNode>kafka(props(
                        "bootstrap.servers", bootstrapServers,
                        "key.deserializer", IntegerDeserializer.class.getName(),
                        "value.deserializer", JsonDeserializer.class.getName(),
                        "auto.offset.reset", AUTO_OFFSET_RESET), TOPIC))
         .withoutTimestamps()
         .peek()
         .map(e -> entry(e.getKey(), e.getValue().toString()))
         .writeTo(Sinks.map(SINK_MAP_NAME));
        return p;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "log4j");
        new KafkaJsonSource().go();
    }

    private void go() throws Exception {
        try {
            if (USE_EMBEDDED_KAFKA) {
                createKafkaCluster();
            }
            topicUtil = new TopicUtil(bootstrapServers);

            createAndFillTopic();

            HazelcastInstance hz = Hazelcast.bootstrappedInstance();
            JetService jet = hz.getJet();

            long start = System.nanoTime();

            Job job = jet.newJob(buildPipeline());

            IMap<Integer, String> sinkMap = hz.getMap(SINK_MAP_NAME);

            while (true) {
                int mapSize = sinkMap.size();
                System.out.format("Received %d entries in %d milliseconds.%n",
                        mapSize, NANOSECONDS.toMillis(System.nanoTime() - start));
                if (mapSize == 20) {
                    SECONDS.sleep(1);
                    cancel(job);
                    break;
                }
                Thread.sleep(100);
            }
        } finally {
            Hazelcast.shutdownAll();
            topicUtil.deleteTopic(TOPIC);
            topicUtil.close();
            if (USE_EMBEDDED_KAFKA) {
                shutdownKafkaCluster();
            }
        }
    }

    private void createAndFillTopic() {
        topicUtil.createTopic(TOPIC, 4);
        Properties props = props(
                "bootstrap.servers", bootstrapServers,
                "key.serializer", IntegerSerializer.class.getName(),
                "value.serializer", JsonSerializer.class.getName());

        ObjectMapper mapper = new ObjectMapper();
        try (KafkaProducer<Integer, JsonNode> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 20; i++) {
                User user = new User("name" + i, "pass" + i, i, i % 2 == 0);
                producer.send(new ProducerRecord<>(TOPIC, i, mapper.valueToTree(user)));
            }
        }
    }

    private void createKafkaCluster() throws IOException {
        LOGGER.info("Creating an embedded zookeeper server and a kafka broker");
        System.setProperty("zookeeper.preAllocSize", Integer.toString(128));
        zkServer = new EmbeddedZookeeper();
        String zkConnect = ZK_HOST + ':' + zkServer.port();
        int brokerPort = randomPort();
        bootstrapServers = BROKER_HOST + ':' + brokerPort;

        KafkaConfig config = new KafkaConfig(props(
                "zookeeper.connect", zkConnect,
                "broker.id", "0",
                "log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString(),
                "listeners", "PLAINTEXT://" + bootstrapServers,
                "offsets.topic.replication.factor", "1",
                "offsets.topic.num.partitions", "1"));
        Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);
    }


    private void shutdownKafkaCluster() {
        kafkaServer.shutdown();
        zkServer.shutdown();
    }

    private static void cancel(Job job) {
        job.cancel();
        while (job.getStatus() != JobStatus.FAILED) {
            uncheckRun(() -> SECONDS.sleep(1));
        }
    }

    private static int randomPort() throws IOException {
        ServerSocket server = null;
        try {
            server = new ServerSocket(0);
            return server.getLocalPort();
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }

    private static Properties props(String... kvs) {
        final Properties props = new Properties();
        for (int i = 0; i < kvs.length; ) {
            props.setProperty(kvs[i++], kvs[i++]);
        }
        return props;
    }

}
