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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static com.hazelcast.jet.kafka.KafkaSources.kafka;
import static com.hazelcast.jet.pipeline.Sinks.map;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A sample which consumes two Kafka topics and writes
 * the received items to an {@code IMap}.
 * <p>
 * This test requires a dockerized Hazelcast instance along with dockerized Confluent Platform:
 * - Download https://raw.githubusercontent.com/confluentinc/cp-all-in-one/7.1.0-post/cp-all-in-one/docker-compose.yml
 * and rename it to confluent.yml
 * - Run docker-compose using confluent.yml and jet/kafka/hazelcast.yml:
 * docker-compose -f confluent.yml -f hazelcast-compose.yml up
 **/
public class KafkaSourceWithStandaloneHZAndKafka {

    private static final ILogger LOGGER = Logger.getLogger(KafkaSourceWithStandaloneHZAndKafka.class);
    private static final int MESSAGE_COUNT_PER_TOPIC = 1_000_000;
    private static final String AUTO_OFFSET_RESET = "earliest";

    private static final String SINK_NAME = "sink";
    private static final String INTERNAL_DOCKER_BROKER_ADDRESS = "broker:29092";
    private static final String EXTERNAL_DOCKER_BROKER_ADDRESS = "localhost:9092";

    private TopicUtil topicUtil;

    private static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();
        //we need to use an internal broker address as the pipeline will be executed in the HZ container
        p.readFrom(kafka(props(
                                "bootstrap.servers", INTERNAL_DOCKER_BROKER_ADDRESS,
                                "key.deserializer", StringDeserializer.class.getCanonicalName(),
                                "value.deserializer", StringDeserializer.class.getCanonicalName(),
                                "auto.offset.reset", AUTO_OFFSET_RESET)
                        , "t1", "t2"))
                .withoutTimestamps()
                .writeTo(map(SINK_NAME));
        return p;
    }

    public static void main(String[] args) {
        new KafkaSourceWithStandaloneHZAndKafka().run();
    }

    private void run() {
        try {
            topicUtil = new TopicUtil(EXTERNAL_DOCKER_BROKER_ADDRESS);

            fillTopics();
            HazelcastInstance hz = HazelcastClient.newHazelcastClient();
            JetService jet = hz.getJet();
            IMap<String, String> sinkMap = hz.getMap(SINK_NAME);
            sinkMap.clear();

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
        } catch (Exception ex) {
            LOGGER.warning(ex);
        } finally {
            HazelcastClient.shutdownAll();
            topicUtil.deleteTopic("t1");
            topicUtil.deleteTopic("t2");
            topicUtil.close();
        }
    }

    // Creates 2 topics (t1, t2) with different partition counts (32, 64) and fills them with items
    private void fillTopics() {
        topicUtil.forceTopicDeletion("t1");
        topicUtil.forceTopicDeletion("t2");
        topicUtil.createTopic("t1", 32);
        topicUtil.createTopic("t2", 64);

        LOGGER.info("Filling Topics");
        Properties props = props(
                "bootstrap.servers", EXTERNAL_DOCKER_BROKER_ADDRESS,
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

    private static Properties props(String... kvs) {
        final Properties props = new Properties();
        for (int i = 0; i < kvs.length; ) {
            props.setProperty(kvs[i++], kvs[i++]);
        }
        return props;
    }
}
