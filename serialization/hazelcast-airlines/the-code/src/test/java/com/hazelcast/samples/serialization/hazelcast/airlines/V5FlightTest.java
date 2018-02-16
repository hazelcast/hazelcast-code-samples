package com.hazelcast.samples.serialization.hazelcast.airlines;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;
import com.hazelcast.spi.serialization.SerializationService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>External serialization test for {@link V5Flight}
 * </p>
 */
@Slf4j
public class V5FlightTest {

	@Test
	public void test_serialization() throws Exception {
		V5Flight objectSent = FlightBuilder.buildV5();
		Object objectReceived = null;
		byte[] bytes;

		SerializerConfig serializerConfig = new SerializerConfig();
		serializerConfig.setTypeClass(V5Flight.class);
		serializerConfig.setClass(V5FlightSerializer.class);
		
		SerializationConfig serializationConfig = new SerializationConfig();
		serializationConfig.addSerializerConfig(serializerConfig);
		
		SerializationService serializationService = 
                new com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder()
                .setConfig(serializationConfig)
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
