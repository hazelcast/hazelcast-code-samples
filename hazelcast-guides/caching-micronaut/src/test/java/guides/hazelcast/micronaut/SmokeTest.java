package guides.hazelcast.micronaut;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class SmokeTest {

    @Inject
    CommandController instance;

    @Test
    void testItWorks() {
        instance.put("foo", "bar");

        assertEquals("bar", instance.get("foo").getValue());
    }
}
