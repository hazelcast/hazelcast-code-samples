package com.hazelcast.app.source.messagebus.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class MessageBusProducer {
	private static final Logger LOGGER = LogManager.getLogger("MessageBusProducer");

	private final static String DELIMITER = ",";

	private final ThreadLocal<String> messageBusIpPort = new ThreadLocal<>();

	public MessageBusProducer(String mbTopicNameIn, String messageBusIpPortIn, String deviceSourcePathIn) {
		messageBusIpPort.set(messageBusIpPortIn);

		try {
			produceRecords(mbTopicNameIn, deviceSourcePathIn);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public void produceRecords(String mbTopicNameIn, String deviceSourcePathIn) {
		try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProps())) {
			Path path = Paths.get(deviceSourcePathIn);
			List<String> lines = Files.readAllLines(path);
			List<String[]> values = lines.stream().skip(1).map(line -> line.split(DELIMITER)).collect(Collectors.toList());

			for (String[] items : values) {
				String patientId = items[0];
				String measurement = items[1];
				if (mbTopicNameIn.equalsIgnoreCase("BREATHING")) {
					if (measurement.equalsIgnoreCase("true")) measurement = "air";
					else measurement = "oxygen";
				} else if (mbTopicNameIn.equalsIgnoreCase("CONSCIOUSNESS")) {
					if (measurement.equalsIgnoreCase("true")) measurement = "alert";
					else measurement = "unalert";
				}
				ProducerRecord<String, String> producerRecord = new ProducerRecord<>(mbTopicNameIn, patientId, measurement);
				producer.send(producerRecord);
				Thread.sleep(100);
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info(new RuntimeException(e).getMessage());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Properties kafkaProps() {
		Properties props = new Properties();
		props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, messageBusIpPort.get());
		props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
		props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
		props.setProperty(ProducerConfig.ACKS_CONFIG, "0");
		return props;
	}

}