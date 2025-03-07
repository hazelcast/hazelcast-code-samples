package guides.hazelcast.tomcatsessionmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    @Test
    public void testSessionReplication() {
        // given
        String port1 = startApplication();
        String port2 = startApplication();

        // when
        ResponseEntity<?> response1 = setAttribute(port1, null);
        String sessionId = extractCookie(response1, "JSESSIONID");
        ResponseEntity<?> response2 = getAttribute(port2, sessionId);

        // then
        String body = response2.getBody().toString();
        assertTrue(body.contains(TEST_VALUE));
    }

    private static String startApplication() {
        return new SpringApplicationBuilder(Application.class)
                .properties("server.port=0")
                .run()
                .getEnvironment()
                .getProperty("local.server.port");
    }

    private static ResponseEntity<?> getAttribute(String port, String sessionId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + "/get")
                .queryParam("key", TEST_KEY);
        return makeRequest(builder.toUriString(), sessionId);
    }

    private static ResponseEntity<?> setAttribute(String port, String sessionId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + "/put")
                .queryParam("key", TEST_KEY)
                .queryParam("value", TEST_VALUE);
        return makeRequest(builder.toUriString(), sessionId);
    }

    private static ResponseEntity<?> makeRequest(String uri, String sessionId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        if (sessionId != null) {
            headers.add("Cookie", String.format("JSESSIONID=%s", sessionId));
        }
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private String extractCookie(ResponseEntity<?> response, String cookie) {
        return response.getHeaders().get("Set-Cookie").stream()
                .filter(s -> s.contains(cookie))
                .map(s -> s.split(";")[0])
                .map(s -> s.substring(cookie.length() + 1))
                .findFirst().orElse(null);
    }

}
