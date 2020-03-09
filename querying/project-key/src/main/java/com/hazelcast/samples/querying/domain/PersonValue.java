package com.hazelcast.samples.querying.domain;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * This is the value for the "{@code person}" map.
 * </P>
 */
@SuppressWarnings("serial")
public class PersonValue implements Serializable {

    private LocalDate dateOfBirth;

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
