package com.hazelcast.app.common.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Resource {
	private static final Logger LOGGER = LogManager.getLogger("Resource");

	private static final String DELIMITER = ",";
	private static final String FILENAME = "hazelcast.properties";

	private static final Resource obj = new Resource();

	private int capacity;
	private String clusterName;
	private String dataPath;
	private String[] deviceSource;
	private String[] hazelcastIpAddress;
	private String[] hazelcastPort;
	private String[] jobConfigName;
	private String[] messageBusIpAddress;
	private String[] messageBusPort;
	private String messageBusSource;
	private String mode;
	private String patientMap;
	private String profileMap;
	private String profileSource;
	private String projectPath;
	private String resultMap;
	private String rulesEngineJob;
	private String rulesEnginePath;
	private String rulesEngineSource;
	private String[] sinkName;
	private String[] sinkIpAddress;
	private String[] sinkPort;
	private String[] topicMapName;

	private Resource() {
		try (InputStream inputStream = Resource.class.getClassLoader().getResourceAsStream(FILENAME)) {
			Properties properties = new Properties();
			if (inputStream == null) {
				LOGGER.info("Sorry, unable to find config.properties");
				return;
			}
			properties.load(inputStream);

			// Set the Hazelcast Properties to the corresponding variable in the Resource class
			setCapacity(Integer.parseInt(properties.getProperty("CAPACITY")));
			setClusterName(properties.getProperty("CLUSTER_NAME").trim());
			setDataPath(properties.getProperty("DATA_PATH").trim());
			setDeviceSource(properties.getProperty("DEVICE_SOURCE_ARR").split(DELIMITER));
			setHazelcastIpAddress(properties.getProperty("HAZELCAST_IP_ADDRESS_ARR").split(DELIMITER));
			setHazelcastPort(properties.getProperty("HAZELCAST_PORT_ARR").split(DELIMITER));
			setJobConfigName(properties.getProperty("JOB_CONFIG_NAME_ARR").split(DELIMITER));
			setMessageBusIpAddress(properties.getProperty("MESSAGE_BUS_IP_ADDRESS_ARR").split(DELIMITER));
			setMessageBusPort(properties.getProperty("MESSAGE_BUS_PORT_ARR").split(DELIMITER));
			setMessageBusSource(properties.getProperty("MESSAGE_BUS_SOURCE").trim());
			setMode(properties.getProperty("MODE").trim());
			setPatientMap(properties.getProperty("PATIENT_MAP").trim());
			setProfileMap(properties.getProperty("PROFILE_MAP").trim());
			setProfileSource(properties.getProperty("PROFILE_SOURCE").trim());
			setProjectPath(properties.getProperty("PROJECT_PATH").trim());
			setResultMap(properties.getProperty("RESULT_MAP").trim());
			setRulesEngineJob(properties.getProperty("RULES_ENGINE_JOB").trim());
			setRulesEnginePath(properties.getProperty("RULES_ENGINE_PATH").trim());
			setRulesEngineSource(properties.getProperty("RULES_ENGINE_SOURCE").trim());
			setSinkName(properties.getProperty("SINK_NAME_ARR").split(DELIMITER));
			setSinkIpAddress(properties.getProperty("SINK_IP_ADDRESS_ARR").split(DELIMITER));
			setSinkPort(properties.getProperty("SINK_PORT_ARR").split(DELIMITER));
			setTopicAndMapName(properties.getProperty("TOPIC_MAP_NAME_ARR").split(DELIMITER));
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public static Resource getInstance() {
		return obj;
	}

	public int getCapacity() {
		return capacity;
	}
	private void setCapacity(int capacityIn) {
		capacity = capacityIn;
	}

	public String getClusterName() {
		return clusterName;
	}
	private void setClusterName(String clusterNameIn) {
		clusterName = clusterNameIn;
	}

	public String getDataPath() {
		return dataPath;
	}
	private void setDataPath(String dataPathIn) {
		dataPath = dataPathIn;
	}

	public String[] getDeviceSource() {
		return deviceSource;
	}
	private void setDeviceSource(String[] deviceSourceIn) {
		deviceSource = deviceSourceIn;
	}

	public String[] getHazelcastIpAddress() {
		return hazelcastIpAddress;
	}
	private void setHazelcastIpAddress(String[] hazelcastIpAddressIn) {
		hazelcastIpAddress = hazelcastIpAddressIn;
	}

	public String[] getHazelcastPort() {
		return hazelcastPort;
	}
	private void setHazelcastPort(String[] hazelcastPortIn) {
		hazelcastPort = hazelcastPortIn;
	}

	public String[] getJobConfigName() {
		return jobConfigName;
	}
	private void setJobConfigName(String[] jobConfigNameIn) {
		jobConfigName = jobConfigNameIn;
	}

	public String[] getMessageBusIpAddress() {
		return messageBusIpAddress;
	}
	private void setMessageBusIpAddress(String[] messageBusIpAddressIn) {
		messageBusIpAddress = messageBusIpAddressIn;
	}

	public String[] getMessageBusPort() {
		return messageBusPort;
	}
	private void setMessageBusPort(String[] messageBusPortIn) {
		messageBusPort = messageBusPortIn;
	}

	public String getMessageBusSource() {
		return messageBusSource;
	}
	private void setMessageBusSource(String messageBusSourceIn) {
		messageBusSource = messageBusSourceIn;
	}

	public String getMode() {
		return mode;
	}
	private void setMode(String modeIn) {
		mode = modeIn;
	}

	public String getPatientMap() {
		return patientMap;
	}
	private void setPatientMap(String patientMapIn) {
		patientMap = patientMapIn;
	}

	public String getProfileMap() {
		return profileMap;
	}
	private void setProfileMap(String profileMapIn) {
		profileMap = profileMapIn;
	}

	public String getProfileSource() {
		return profileSource;
	}
	private void setProfileSource(String profileSourceIn) {
		profileSource = profileSourceIn;
	}

	public String getProjectPath() {
		return projectPath;
	}
	private void setProjectPath(String projectPathIn) {
		projectPath = projectPathIn;
	}

	public String getResultMap() {
		return resultMap;
	}
	private void setResultMap(String resultMapIn) {
		resultMap = resultMapIn;
	}

	public String getRulesEngineJob() {
		return rulesEngineJob;
	}
	private void setRulesEngineJob(String rulesEngineJobIn) {
		rulesEngineJob = rulesEngineJobIn;
	}

	public String getRulesEnginePath() {
		return rulesEnginePath;
	}
	private void setRulesEnginePath(String rulesEnginePathIn) {
		rulesEnginePath = rulesEnginePathIn;
	}

	public String getRulesEngineSource() {
		return rulesEngineSource;
	}
	private void setRulesEngineSource(String rulesEngineSourceIn) {
		rulesEngineSource = rulesEngineSourceIn;
	}

	public String[] getSinkName() {
		return sinkName;
	}
	private void setSinkName(String[] sinkNameIn) {
		sinkName = sinkNameIn;
	}

	public String[] getSinkIpAddress() {
		return sinkIpAddress;
	}
	private void setSinkIpAddress(String[] sinkIpAddressIn) {
		sinkIpAddress = sinkIpAddressIn;
	}

	public String[] getSinkPort() {
		return sinkPort;
	}
	private void setSinkPort(String[] sinkPortIn) {
		sinkPort = sinkPortIn;
	}

	public String[] getTopicAndMapName() {
		return topicMapName;
	}
	private void setTopicAndMapName(String[] topicAndMapNameIn) {
		topicMapName = topicAndMapNameIn;
	}

}