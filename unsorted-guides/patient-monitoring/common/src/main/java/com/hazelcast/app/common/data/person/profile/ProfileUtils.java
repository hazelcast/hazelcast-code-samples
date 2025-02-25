package com.hazelcast.app.common.data.person.profile;

import com.hazelcast.app.common.connection.ClientConnection;
import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ProfileUtils {
	private static final Logger LOGGER = LogManager.getLogger("ProfileUtils");

	private static final ProfileUtils obj = new ProfileUtils();

	public static ProfileUtils getInstance() {
		return obj;
	}

	private Map<String, Profile> getProfileMap() {
		Resource resource = Resource.getInstance();
		HazelcastInstance hazelcastInstance = ClientConnection.getInstance().connect("common", resource.getClusterName(),resource.getHazelcastIpAddress()[0]);
		return hazelcastInstance.getMap(resource.getProfileMap());
	}

	public Profile getProfile(String profileIdIn) {
		Map<String, Profile> profileMap = getProfileMap();
		return new Profile(
				profileMap.get(profileIdIn).getProfileId(),
				profileMap.get(profileIdIn).getFirstName(),
				profileMap.get(profileIdIn).getMiddleName(),
				profileMap.get(profileIdIn).getLastName(),
				profileMap.get(profileIdIn).getGender(),
				LocalDateTime.ofEpochSecond(profileMap.get(profileIdIn).getBirthDate(), 0, ZoneOffset.UTC),
				profileMap.get(profileIdIn).getLocation(),
				Boolean.parseBoolean(String.valueOf(profileMap.get(profileIdIn).isActive())),
				LocalDateTime.ofEpochSecond(profileMap.get(profileIdIn).getCreatedDttm(), 0, ZoneOffset.UTC)
		);
	}

	private void loadProfileMap(Map<String, Profile> mapIn, String dataPathIn, String profileSourceIn) {
		Map<String, Profile> result;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(dataPathIn.concat("/").concat(profileSourceIn)))))) {
			result = reader.lines()
					.skip(1)
					.map(line -> line.split(","))
					.collect(toMap(entry -> entry[0], entry -> new Profile(
											entry[0],
											entry[1],
											entry[2],
											entry[3],
											entry[4],
											LocalDateTime.parse(entry[5]),
											entry[6],
											Boolean.parseBoolean(entry[7]),
											LocalDateTime.parse(entry[8]
											)
									)
							)
					);
			mapIn.putAll(result);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	public void createProfileMap(HazelcastInstance hazelcastInstanceIn, String profileMapIn, String dataPathIn, String profileSourceIn) {
		Map<String, Profile> map = hazelcastInstanceIn.getMap(profileMapIn);
		loadProfileMap(map, dataPathIn, profileSourceIn);
	}

}