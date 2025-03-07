package com.hazelcast.app.source.messagebus;

import com.hazelcast.app.source.messagebus.consumer.MessageBusConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageBusConsumerFactory implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger("MessageBusConsumerFactory");

	private final String messageBusTopicName;
	private final String messageBusIpPort;

	public MessageBusConsumerFactory(String messageBusTopicNameIn, String messageBusIpPortIn) {
		messageBusTopicName = messageBusTopicNameIn;
		messageBusIpPort = messageBusIpPortIn;
	}

	@Override
	public synchronized void run() {
		try {
			new MessageBusConsumer(messageBusTopicName, messageBusIpPort);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(new RuntimeException(e).getMessage());
		}
	}

}