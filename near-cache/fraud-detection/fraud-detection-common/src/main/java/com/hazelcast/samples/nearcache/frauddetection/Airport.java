package com.hazelcast.samples.nearcache.frauddetection;

import java.io.Serializable;

import lombok.Data;

/**
 * <P>The domain model to hold the location of airports.
 * Although they are not all at sea-level, we don't care
 * about their altitude.
 * </P>
 * <P><B>NOTE</B> The selected serialization mechanism
 * here is Java provided {@code java.io.Serializable}.
 * This is usually the slowest serialization mechanism,
 * but once the data is held in a near-cache the
 * transfer time from instance to instance is irrelevant.
 * </P>
 */
@SuppressWarnings("serial")
@Data
public class Airport implements Serializable {

	private String code;
	private String decription;
	private double latitude;
	private double longitude;
	
}
