package com.hazelcast.samples.serialization.hazelcast.airlines;

import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * <p>Hazelcast serialization test for {@link V3Flight}
 * </p>
 */
@Slf4j
public class V3FlightTest {

    @Test
    public void test_serialization() throws Exception {
        V3Flight objectSent = FlightBuilder.buildV3();
        Object objectReceived = null;
        byte[] bytes;

        SerializationService serializationService =
                new com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder()
                        .build();

        Data data = serializationService.toData(objectSent);
        bytes = data.toByteArray();

        objectReceived = serializationService.toObject(data);

        // We should get back a different object of the same type and content
        assertThat(objectReceived, notNullValue());
        assertThat(objectReceived, instanceOf(objectSent.getClass()));
        assertThat("Identity", System.identityHashCode(objectReceived),
                not(equalTo(System.identityHashCode(objectSent))));
        assertThat("Equality", objectReceived, equalTo(objectSent));

        log.info("====================================================================");
        log.info(objectReceived.getClass().getName());
        log.info("====================================================================");
        log.info("Bytes for object serialized: {}", bytes.length);
        log.info("====================================================================");
        log.info(Arrays.toString(bytes));
        log.info("====================================================================");
        log.info(new String(bytes));
        log.info("====================================================================");
        log.info(objectReceived.toString());
        log.info("====================================================================");
    }

}
