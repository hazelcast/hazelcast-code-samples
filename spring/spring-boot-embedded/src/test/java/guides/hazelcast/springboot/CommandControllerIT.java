package guides.hazelcast.springboot;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = HazelcastApplication.class)
class CommandControllerIT {

    private RestTestClient restTestClient;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private Map<String, String> keyValueMap;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void testPutRequest() {
        //when
        RestTestClient.ResponseSpec responseSpec = makePutRequest("key1", "value1");

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
    void testGetRequest() {
        //given
        makePutRequest("key1", "value1");

        //when
        RestTestClient.ResponseSpec responseSpec = restTestClient
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

    private RestTestClient.ResponseSpec makePutRequest(Object... parameters) {
        return restTestClient
                .post()
                .uri("/put?key={key}&value={value}", parameters)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange();
    }

    @Test
    void testHazelcastCluster() {
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
