package com.hazelcast.app.source.messagebus;

import com.hazelcast.app.source.messagebus.producer.MessageBusProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageBusProducerFactory implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger("MessageBusProducerFactory");

	private final String mbTopicName;
	private final String messageBusIpPort;
	private final String deviceSourcePath;

	public MessageBusProducerFactory(String messageBusTopicNameIn, String messageBusIpPortIn, String deviceSourcePathIn) {
		mbTopicName = messageBusTopicNameIn;
		messageBusIpPort = messageBusIpPortIn;
		deviceSourcePath = deviceSourcePathIn;
	}

	@Override
	public synchronized void run() {
		try {
			new MessageBusProducer(mbTopicName, messageBusIpPort, deviceSourcePath);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(new RuntimeException(e).getMessage());
		}
	}

}