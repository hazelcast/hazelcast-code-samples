package com.hazelcast.guide;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HazelcastSpringSessionApplicationTests {

	static final String COOKIE_NAME = "SESSION";

	@Test
	void contextLoads() {
		// given
		String port1 = startApplication();
		String port2 = startApplication();
		Map<String, String> principalMap = Collections.singletonMap("principal", "hazelcast2020");

		// when
		ResponseEntity<?> response1 = makeRequest(port1, "create", null, principalMap);
		String sessionCookie1 = extractSessionCookie(response1);

		// then
		ResponseEntity<?> response2 = makeRequest(port2, "create", sessionCookie1, principalMap);
		assertTrue(response2.getBody().toString().contains("Session already exists"));

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
				.findFirst().orElse(null);
	}

	private static ResponseEntity<?> makeRequest(String port, String endpoint, String sessionCookie, Map<String, String> parameters) {
		String url = "http://localhost:" + port + "/" + endpoint;

		// Header
		HttpHeaders headers = new HttpHeaders();
		if (sessionCookie != null) {
			headers.add("Cookie", COOKIE_NAME + "=" + sessionCookie);
		}

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		parameters.forEach(builder::queryParam);

		// Request
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
	}

}
