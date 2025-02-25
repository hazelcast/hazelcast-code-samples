package com.hazelcast.app.stream.pipeline;

import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PipelineFactory {
	private static final Logger LOGGER = LogManager.getLogger("PipelineFactory");

	private final Resource resource;
	private final HazelcastInstance hazelcastInstance;
	private final String hazelcastIpAddress;
	private final String patientMap;

	public PipelineFactory(HazelcastInstance hazelcastInstanceIn, String hazelcastIpAddressIn, String patientMapIn) {
		resource = Resource.getInstance();
		hazelcastInstance = hazelcastInstanceIn;
		hazelcastIpAddress = hazelcastIpAddressIn;
		patientMap = patientMapIn;

		try {
			createPipelineImpl();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public void createPipelineImpl() {
		String[] topicNameArr = resource.getTopicAndMapName();
		String[] jobConfigNameArr = resource.getJobConfigName();
		String sinkIpAddress = resource.getSinkIpAddress()[0];
		String sinkPort = resource.getSinkPort()[0];
		String messageBusIpAddress = resource.getMessageBusIpAddress()[0];
		String messageBusPort = resource.getMessageBusPort()[0];
		int size = topicNameArr.length;

		try {
			for (int i = 0; i < size; i++) {
				new PipelineImpl(
						hazelcastInstance,
						hazelcastIpAddress,
						patientMap,
						topicNameArr[i],
						jobConfigNameArr[i],
						sinkIpAddress,
						sinkPort,
						messageBusIpAddress.concat(":").concat(messageBusPort));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

}