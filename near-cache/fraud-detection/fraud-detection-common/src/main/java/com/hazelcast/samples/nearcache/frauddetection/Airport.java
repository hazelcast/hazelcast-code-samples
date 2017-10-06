package com.hazelcast.samples.nearcache.frauddetection;

import lombok.Data;

import java.io.Serializable;

/**
 * The domain model to hold the location of airports.
 * Although they are not all at sea-level, we don't care
 * about their altitude.
 * <p>
 * <b>Note:</b> The selected serialization mechanism
 * here is Java provided {@code java.io.Serializable}.
 * This is usually the slowest serialization mechanism,
 * but once the data is held in a Near Cache the
 * transfer time from instance to instance is irrelevant.
 */
@SuppressWarnings("serial")
@Data
public class Airport implements Serializable {

    private String code;
    private String description;
    private double latitude;
    private double longitude;
}
