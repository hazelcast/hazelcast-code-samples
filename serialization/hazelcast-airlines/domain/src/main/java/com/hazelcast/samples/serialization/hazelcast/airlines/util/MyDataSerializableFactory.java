package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.samples.serialization.hazelcast.airlines.V4Flight;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>On the receiving process, builds {@link com.hazelcast.nio.serialization.IdentifiedDataSerializable
 * IdentifiedDataSerializable} objects based on the code number sent by the sending process.
 * </p>
 */
@Slf4j
public class MyDataSerializableFactory implements DataSerializableFactory {

	@Override
	public IdentifiedDataSerializable create(int id) {
		switch (id) {
			case Constants.V4FLIGHT_ID: return new V4Flight();
		}
		log.error("create({}), unknown code", id);
		return null;
	}

}
