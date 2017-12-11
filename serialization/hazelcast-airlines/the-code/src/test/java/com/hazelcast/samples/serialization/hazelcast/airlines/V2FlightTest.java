package com.hazelcast.samples.serialization.hazelcast.airlines;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.junit.Test;

import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Java serialization test for {@link V2Flight}
 * </p>
 */
@Slf4j
public class V2FlightTest {

	@Test
	public void test_serialization() throws Exception {
		V2Flight objectSent = FlightBuilder.buildV2();
		Object objectReceived = null;
		byte[] bytes;

		// Serialize
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);) {

			objectOutputStream.writeObject(objectSent);
			bytes = byteArrayOutputStream.toByteArray();
		}

		// De-Serialize
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);) {
			objectReceived = objectInputStream.readObject();
		}

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
