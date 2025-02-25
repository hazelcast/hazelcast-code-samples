package com.hazelcast.guide;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpMethod.GET;

@SuppressWarnings("DataFlowIssue")
class HazelcastSpringSessionApplicationIT {

	private static final Logger logger = LoggerFactory.getLogger(HazelcastSpringSessionApplicationIT.class);

	static final String COOKIE_NAME = "SESSION";

	@Test
	void contextLoads() {
		// given
		String port1 = startApplication();
		logger.info("Started 1st on port: {}", port1);
		String port2 = startApplication();
		logger.info("Started 2nd on port: {}", port2);
		Map<String, String> principalMap = Collections.singletonMap("principal", "hazelcast2020");

		waitForCluster(port1);

		// when
		ResponseEntity<?> response1 = makeRequest(port1, "create", null, principalMap);
		String sessionCookie1 = extractSessionCookie(response1);
		logger.info("First session cookie: {}", sessionCookie1);

		// then
		ResponseEntity<?> response2 = makeRequest(port2, "create", sessionCookie1, principalMap);
		String body = response2.getBody().toString();
        logger.info("Body of second query contains: {}", body);

		assertThat(body).contains("Session already exists");
	}

	private static String startApplication() {
		return new SpringApplicationBuilder(HazelcastSpringSessionApplication.class)
				.properties("server.port=0")
				.run()
				.getEnvironment()
				.getProperty("local.server.port");
	}

	private String extractSessionCookie(ResponseEntity<?> response) {
		return response.getHeaders().get("Set-Cookie").stream()
				.filter(s -> s.contains(COOKIE_NAME))
				.map(s -> s.substring(COOKIE_NAME.length() + 1))
				.map(s -> s.contains(";") ? s.substring(0, s.indexOf(";")) : s)
				.map(s -> new String(Base64.getDecoder().decode(s)))
				.findFirst().orElse(null);
	}

	@SuppressWarnings("SameParameterValue")
    private static ResponseEntity<?> makeRequest(String port, String endpoint, String sessionCookie, Map<String, String> parameters) {
		String url = "http://localhost:" + port + "/" + endpoint;

		// Header
		HttpHeaders headers = new HttpHeaders();
		if (sessionCookie != null) {
			headers.add("Cookie", COOKIE_NAME + "=" +
					Base64.getEncoder().encodeToString(sessionCookie.getBytes(UTF_8)));
		}

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		parameters.forEach(builder::queryParam);

		// Request
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(builder.toUriString(), GET, new HttpEntity<>(headers), String.class);
	}

	public static void waitForCluster(String port) {
		String url = "http://localhost:" + port + "/clusterSize";

		var restTemplate = new RestTemplate();
		var requestEntity = new HttpEntity<>(new HttpHeaders());

		await()
				.atMost(Duration.ofMinutes(2))
				.pollInterval(Duration.ofSeconds(2))
				.logging(logger::info)
				.alias("Waiting for correct cluster size")
				.until(() -> {
					ResponseEntity<Integer> clusterSize = restTemplate.exchange(url, GET, requestEntity, Integer.class);
					return clusterSize.getBody() == 2;
				});
	}

}
