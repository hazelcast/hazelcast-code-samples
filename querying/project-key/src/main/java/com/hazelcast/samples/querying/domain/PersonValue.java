package com.hazelcast.samples.querying.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <P>
 * This is the value for the "{@code person}" map.
 * </P>
 */
@Data
@SuppressWarnings("serial")
public class PersonValue implements Serializable {

    private LocalDate dateOfBirth;

}
