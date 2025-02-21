package com.hazelcast.springboot.caching;

import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("embedded")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApplicationEmbeddedTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Test
    void useCachedValue() {
        // given
        String isbn = "12345";
        String cachedValue = "cached-value";
        hazelcastInstance.getCacheManager().getCache("books").put(isbn, cachedValue);

        // when
        String response = restTemplate.getForObject(String.format("http://localhost:%s/books/%s", port, isbn), String.class);

        // then
        assertThat(response).isEqualTo(cachedValue);
    }
}
