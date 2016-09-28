package com.hazelcast.samples.spring.data.chemistry.repository;

import java.util.List;

import org.springframework.data.keyvalue.repository.KeyValueRepository;

import com.hazelcast.samples.spring.data.chemistry.domain.Isotope;
import com.hazelcast.samples.spring.data.chemistry.domain.IsotopeKey;

/**
 * <P>An {@code IsotopeRepository} subtypes 
 * {@link org.springframework.data.keyvalue.repository.KeyValueRepository}
 * providing its features for the {@link Isotope} domain model.
 * </P>
 * <P>
 * It would be equally valid for this to extend 
 * {@link org.springframework.data.hazelcast.repository.HazelcastRepository}
 * to more strongly associate the repository with the implementation.
 * </P>
 * <P>
 * Spring can deduce the implementation of the two methods {@code findByIsotopeKeySymbol()}
 * and {@code findByIsotopeKeyAtomicWeight()} from the method name.
 */
public interface IsotopeRepository extends KeyValueRepository<Isotope, IsotopeKey> {
	
	public List<Isotope> 	findByIsotopeKeySymbol(String s);
	
	public List<Isotope>	findByIsotopeKeyAtomicWeight(int i);
}
