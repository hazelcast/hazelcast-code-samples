package com.hazelcast.app.source;

import com.hazelcast.app.common.resource.Resource;
//import com.hazelcast.app.source.messagebus.MessageBusConsumerFactory;
import com.hazelcast.app.source.messagebus.MessageBusProducerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger("Main");

	public static void main(String[] args) {
		Resource resource = Resource.getInstance();
		String dataPath = resource.getDataPath();
		String[] deviceSourceArr = resource.getDeviceSource();
		String messageBusIpAddress = resource.getMessageBusIpAddress()[0];
		String messageBusPort = resource.getMessageBusPort()[0];
		String[] topicMapNameArr = resource.getTopicAndMapName();

		try {
			String messageBusIpPort = messageBusIpAddress.concat(":").concat(messageBusPort);
			startMessageBusFactory(topicMapNameArr, messageBusIpPort, dataPath, deviceSourceArr);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	private static void startMessageBusFactory(
			String[] topicMapNameArrIn,
			String messageBusIpPortIn,
			String dataPathIn,
			String[] deviceSourceArrIn
	) {
		int size = topicMapNameArrIn.length;
		ExecutorService executor = Executors.newFixedThreadPool(size*2);

		try {
			for (int i = 0; i < size; i++) {
				String deviceSourcePath = dataPathIn.concat("/").concat(deviceSourceArrIn[i]);
				executor.execute(new MessageBusProducerFactory(topicMapNameArrIn[i], messageBusIpPortIn, deviceSourcePath));
//				executor.execute(new MessageBusConsumerFactory(topicMapNameArrIn[i], messageBusIpPortIn));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

}