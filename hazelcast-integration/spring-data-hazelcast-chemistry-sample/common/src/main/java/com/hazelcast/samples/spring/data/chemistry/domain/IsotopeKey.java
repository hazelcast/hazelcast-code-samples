package com.hazelcast.samples.spring.data.chemistry.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Follow standard chemistry practice by using the element's symbol
 * and atomic weight to define the key to an isotope.
 */
@Data
public class IsotopeKey implements Comparable<IsotopeKey>, Serializable {

    private static final long serialVersionUID = 1L;

    private String symbol;
    private int atomicWeight;

    public int compareTo(IsotopeKey that) {
        int symbolCompare = this.symbol.compareTo(that.getSymbol());
        return (symbolCompare != 0 ? symbolCompare : (this.atomicWeight - that.getAtomicWeight()));
    }
}
