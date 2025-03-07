package com.hazelcast.app.stream.rulesengine;

import com.hazelcast.app.common.connection.ClientConnection;
import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger("Main");

	public static void main(String[] args) {
		Resource resource = Resource.getInstance();
		String clusterName = resource.getClusterName();
		String hazelcastIpAddress = resource.getHazelcastIpAddress()[0];
		String patientMap = resource.getPatientMap();

		try {
			HazelcastInstance hazelcastInstance = ClientConnection.getInstance().connect("rulesengine", clusterName, hazelcastIpAddress);
			startRulesEngine(hazelcastInstance, hazelcastIpAddress, patientMap);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	private static void startRulesEngine(
			HazelcastInstance hazelcastInstanceIn,
			String hazelcastIpAddressIn,
			String patientMapIn
	) {
		try {
			new RulesEngine(hazelcastInstanceIn, hazelcastIpAddressIn, patientMapIn);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

}