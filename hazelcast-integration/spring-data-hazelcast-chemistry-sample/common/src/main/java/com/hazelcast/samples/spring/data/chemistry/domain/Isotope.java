package com.hazelcast.samples.spring.data.chemistry.domain;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import com.hazelcast.samples.spring.data.chemistry.Constants;

import lombok.Data;

/**
 * <P>Some {@link Element} types have more than one form, known as isotopes.
 * </P>
 * <P>Isotopes of an element share the same number of protons and so behave
 * similarly, but differ in the number of neutrons. The atomic weight is the
 * sum of protons and neutrons.
 * </P>
 * <P>The key of the isotope is a composite of the symbol and weight, defined
 * as {@link IsotopeKey}.
 * </P>
 * <P>There are currently no other fields in this class.
 * </P>
 */
@Data
@KeySpace(Constants.KEYSPACE_ISOTOPE)
public class Isotope implements Comparable<Isotope>, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private IsotopeKey	isotopeKey;
    
    public int compareTo(Isotope that) {
    	return this.compareTo(that);
    }
    
}
