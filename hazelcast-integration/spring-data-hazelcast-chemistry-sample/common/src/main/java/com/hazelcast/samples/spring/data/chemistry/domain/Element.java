package com.hazelcast.samples.spring.data.chemistry.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import com.hazelcast.samples.spring.data.chemistry.Constants;

import lombok.Data;

/**
 * <P>An element from the periodic table.
 * </P>
 * <P>Elements are uniquely identified by the atomic number (the number of protons
 * it has), the shorthand symbol and the name. Any or all of these could function
 * as the key. Select one and designate this with the {@code @Id} tag.
 * </P>
 * <P>See also {@link Isotope}, for variants of some elements.
 * </P>
 */
@Data
@KeySpace(Constants.KEYSPACE_ELEMENT)
public class Element implements Comparable<Element>, Serializable {
    private static final long serialVersionUID = 1L;

    private int         atomicNumber;
    private Integer     group;
    private String      name;
    private int         period;
    @Id
    private String      symbol;

    public int compareTo(Element that) {
        return this.symbol.compareTo(that.getSymbol());
    }
    
}
