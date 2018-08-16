package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>The key for a flight object. Keys here are
 * {@link java.io.Serializable}, just for simplicity.
 * </p>
 */
@AllArgsConstructor
@Data
@SuppressWarnings("serial")
public class MyKey implements Comparable<MyKey>, Serializable {

    private String code;
    private LocalDate date;

    public MyKey() {
    }

    /**
     * <p>Sort ascending, on code then date
     * </p>
     *
     * @param that Another such key
     */
    @Override
    public int compareTo(MyKey that) {

        if (!this.getCode().equals(that.getCode())) {
            return this.getCode().compareTo(that.getCode());
        } else {
            return this.getDate().compareTo(that.getDate());
        }

    }
}
