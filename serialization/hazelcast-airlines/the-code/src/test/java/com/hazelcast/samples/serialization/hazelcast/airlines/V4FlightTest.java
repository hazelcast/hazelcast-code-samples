package com.hazelcast.samples.serialization.hazelcast.airlines;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.hazelcast.nio.serialization.Data;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.MyDataSerializableFactory;
import com.hazelcast.spi.serialization.SerializationService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Hazelcast serialization test for {@link V4Flight}
 * </p>
 */
@Slf4j
public class V4FlightTest {

	@Test
	public void test_serialization() throws Exception {
		V4Flight objectSent = FlightBuilder.buildV4();
		Object objectReceived = null;
		byte[] bytes;

        SerializationService serializationService = 
                new com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder()
                .addDataSerializableFactory(Constants.MY_DATASERIALIZABLE_FACTORY, new MyDataSerializableFactory())
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
