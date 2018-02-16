package com.hazelcast.samples.serialization.hazelcast.airlines;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.junit.Test;

import com.hazelcast.nio.serialization.Data;
import com.hazelcast.samples.serialization.hazelcast.airlines.ep.SeatReservationEntryProcessor;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.MyDataSerializableFactory;
import com.hazelcast.spi.serialization.SerializationService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Hazelcast serialization test for {@link SeatReservationEntryProcessor}.
 * It uses {@link com.hazelcast.nio.serialization.Portable Portable} so
 * we should test it.
 * </p>
 */
@Slf4j
public class SeatReservationEntryProcessorTest {

	@Test
	public void test_serialization() throws Exception {
		SeatReservationEntryProcessor objectSent = new SeatReservationEntryProcessor("junit");
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
		log.info(new String(bytes));
		log.info("====================================================================");
		log.error(objectReceived.toString());
		log.info("====================================================================");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_business_logic_empty_plane() throws Exception {
		SeatReservationEntryProcessor seatReservationEntryProcessor = new SeatReservationEntryProcessor("junit");
		
		AbstractFlight v1Flight = new V1Flight();
		Person[][] rows = new Person[Constants.ROWS][Constants.SEATS];
		v1Flight.setRows(rows);
		
		Entry<MyKey, AbstractFlight> entry = new SimpleEntry(null, v1Flight);

		String result = seatReservationEntryProcessor.process(entry);
		
		assertThat(result, notNullValue());
		assertThat("Last row picked of 20x6 empty", result, equalTo("Row 19 Seat F"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_business_logic_full_plane() throws Exception {
		SeatReservationEntryProcessor seatReservationEntryProcessor = new SeatReservationEntryProcessor("junit");
		
		AbstractFlight v1Flight = new V1Flight();
		Person[][] rows = new Person[Constants.ROWS][Constants.SEATS];
		Person someone = new Person();
		someone.setName("SOMEONE");
		for (int i=0 ; i<rows.length; i++) {
			for (int j=0; j<rows[i].length; j++) {
				rows[i][j] = someone;
			}
		}
		v1Flight.setRows(rows);
		
		Entry<MyKey, AbstractFlight> entry = new SimpleEntry(null, v1Flight);

		String result = seatReservationEntryProcessor.process(entry);
		
		assertThat(result, nullValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_business_logic_plane_with_spare_seats() throws Exception {
		SeatReservationEntryProcessor seatReservationEntryProcessor = new SeatReservationEntryProcessor("junit");
		
		AbstractFlight v1Flight = FlightBuilder.buildV1();
		
		Entry<MyKey, AbstractFlight> entry = new SimpleEntry(null, v1Flight);

		String result = seatReservationEntryProcessor.process(entry);
		
		assertThat(result, notNullValue());
	}

}
