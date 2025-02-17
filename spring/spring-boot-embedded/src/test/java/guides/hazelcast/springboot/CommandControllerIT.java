package guides.hazelcast.springboot;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = HazelcastApplication.class)
public class CommandControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private Map<String, String> keyValueMap;

    @Test
    public void testPutRequest() {
        //when
        WebTestClient.ResponseSpec responseSpec = makePutRequest("key1", "value1");

        //then
        responseSpec.expectStatus()
                    .is2xxSuccessful()
                    .expectHeader()
                    .contentType(APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.value").isEqualTo("value1");

        assertThat(keyValueMap).containsEntry("key1", "value1");
    }

    @Test
    public void testGetRequest() {
        //given
        makePutRequest("key1", "value1");

        //when
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri("/get?key={key}", "key1")
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange();

        //then
        responseSpec.expectStatus()
                    .is2xxSuccessful()
                    .expectHeader()
                    .contentType(APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.value").isEqualTo("value1");
    }

    private WebTestClient.ResponseSpec makePutRequest(Object... parameters) {
        return webTestClient
                .post()
                .uri("/put?key={key}&value={value}", parameters)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange();
    }

    @Test
    public void testHazelcastCluster() {
        //given
        Config config = Config.load();
        var hz = Hazelcast.newHazelcastInstance(config);

        //then
        try {
            await()
                    .atMost(Duration.ofMinutes(2))
                    .until(() -> hazelcastInstance.getCluster().getMembers().size() == 2);
        } finally {
            hz.shutdown();
        }
    }
}
