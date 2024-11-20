package com.hazelcast.samples.spring.data.chemistry.domain;

import com.hazelcast.samples.spring.data.chemistry.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.io.Serializable;

/**
 * An element from the periodic table.
 *
 * Elements are uniquely identified by the atomic number (the number of protons
 * it has), the shorthand symbol and the name. Any or all of these could function
 * as the key. Select one and designate this with the {@code @Id} tag.
 *
 * See also {@link Isotope}, for variants of some elements.
 */
@Data
@KeySpace(Constants.KEYSPACE_ELEMENT)
public class Element implements Comparable<Element>, Serializable {

    private static final long serialVersionUID = 1L;

    private int atomicNumber;
    private Integer group;
    private String name;
    private int period;
    @Id
    private String symbol;

    public int compareTo(Element that) {
        return this.symbol.compareTo(that.getSymbol());
    }
}
