package com.hazelcast.springboot.caching;

import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Test
    void useCachedValue() {
        // given
        RestTestClient restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String isbn = "12345";
        String cachedValue = "cached-value";
        hazelcastInstance.getMap("books").put(isbn, cachedValue);

        // when
        String response = restTestClient.get().uri(String.format("http://localhost:%s/books/%s", port, isbn))
                                        .exchange()
                                        .returnResult(String.class)
                                        .getResponseBody();

        // then
        assertThat(response).isEqualTo(cachedValue);
    }
}
