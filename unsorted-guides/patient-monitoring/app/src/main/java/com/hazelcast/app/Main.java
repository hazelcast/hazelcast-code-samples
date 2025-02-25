package com.hazelcast.app;

import com.hazelcast.app.common.connection.ClientConnection;
import com.hazelcast.app.common.data.person.patient.PatientUtils;
import com.hazelcast.app.common.data.person.profile.ProfileUtils;
import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger("Main");

	public static void main(String[] args) {
		Resource resource = Resource.getInstance();
		String clusterName = resource.getClusterName();
		int capacity = resource.getCapacity();
		String dataPath = resource.getDataPath();
		String hazelcastIpAddress = resource.getHazelcastIpAddress()[0];
		String patientMap = resource.getPatientMap();
		String profileMap = resource.getProfileMap();
		String profileSource = resource.getProfileSource();

		try {
			HazelcastInstance hazelcastInstance = ClientConnection.getInstance().connect("app", clusterName, hazelcastIpAddress);
			createPatientMap(hazelcastInstance, capacity, patientMap);
			createProfileMap(hazelcastInstance, profileMap, dataPath, profileSource);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	private static void createPatientMap(HazelcastInstance hazelcastInstanceIn, int capacityIn, String patientMapIn) {
		try {
			PatientUtils.getInstance().createPatientMap(hazelcastInstanceIn, capacityIn, patientMapIn);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	private static void createProfileMap(HazelcastInstance hazelcastInstanceIn, String profileMapIn, String dataPathIn, String profileSourceIn) {
		try {
			ProfileUtils.getInstance().createProfileMap(hazelcastInstanceIn, profileMapIn, dataPathIn, profileSourceIn);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

}