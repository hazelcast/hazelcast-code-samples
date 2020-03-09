package com.hazelcast.samples.querying.domain;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * This is the value for the "{@code life}" map.
 * </P>
 */
@SuppressWarnings("serial")
public class LifeValue implements Serializable {

    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }
}
