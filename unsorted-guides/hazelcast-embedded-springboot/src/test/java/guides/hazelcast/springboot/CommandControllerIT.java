package guides.hazelcast.springboot;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommandControllerIT {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Test
    public void testPutRequest(){
        //when
        WebTestClient.ResponseSpec responseSpec = makePostRequest("/put?key={key}&value={value}", "key1", "value1");

        //then
        responseSpec.expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo("value1");
    }

    @Test
    public void testGetRequest(){
        //given
        makePostRequest("/put?key={key}&value={value}", "key1", "value1");

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

    private WebTestClient.ResponseSpec makePostRequest(String uri, Object... parameters) {
        return webTestClient
                .post()
                .uri(uri, parameters)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange();
    }

    @Test
    public void testHazelcastCluster(){
        //given
        Hazelcast.newHazelcastInstance();

        //then
        assertEquals(2, hazelcastInstance.getCluster().getMembers().size());
    }
}
