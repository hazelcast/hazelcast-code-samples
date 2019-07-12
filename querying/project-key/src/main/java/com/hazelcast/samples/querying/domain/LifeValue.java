package com.hazelcast.samples.querying.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <P>
 * This is the value for the "{@code life}" map.
 * </P>
 */
@Data
@SuppressWarnings("serial")
public class LifeValue implements Serializable {

    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;

}
