package com.hazelcast.app.source.messagebus.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MessageBusConsumer {
	private static final Logger LOGGER = LogManager.getLogger("MessageBusConsumer");

	private final ThreadLocal<String> mbTopicName = new ThreadLocal<>();

	private final ThreadLocal<String> messageBusIpPort = new ThreadLocal<>();

	public MessageBusConsumer(String mbTopicNameIn, String messageBusIpPortIn) {
		mbTopicName.set(mbTopicNameIn);
		messageBusIpPort.set(messageBusIpPortIn);

		try {
			consumeRecords();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public synchronized void consumeRecords() {
		LOGGER.info("consumeRecords");
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps())) {
			consumer.subscribe(Collections.singletonList(mbTopicName.get()));
			// poll for new data
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

				for (ConsumerRecord<String, String> record : records) {
					LOGGER.info("Key: " + record.key() + ", Value: " + record.value());
					LOGGER.info("Partition: " + record.partition() + ", Offset:" + record.offset());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public Properties kafkaProps() {
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, mbTopicName.get());
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, messageBusIpPort.get());
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		return props;
	}

}