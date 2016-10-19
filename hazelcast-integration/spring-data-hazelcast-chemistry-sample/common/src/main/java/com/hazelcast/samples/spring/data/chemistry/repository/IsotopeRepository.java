package com.hazelcast.samples.spring.data.chemistry.repository;

import com.hazelcast.samples.spring.data.chemistry.domain.Isotope;
import com.hazelcast.samples.spring.data.chemistry.domain.IsotopeKey;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

import java.util.List;

/**
 * An {@code IsotopeRepository} subtypes
 * {@link org.springframework.data.keyvalue.repository.KeyValueRepository}
 * providing its features for the {@link Isotope} domain model.
 *
 * It would be equally valid for this to extend
 * {@link org.springframework.data.hazelcast.repository.HazelcastRepository}
 * to more strongly associate the repository with the implementation.
 *
 * Spring can deduce the implementation of the two methods {@code findByIsotopeKeySymbol()}
 * and {@code findByIsotopeKeyAtomicWeight()} from the method name.
 */
public interface IsotopeRepository extends KeyValueRepository<Isotope, IsotopeKey> {

    List<Isotope> findByIsotopeKeySymbol(String s);

    List<Isotope> findByIsotopeKeyAtomicWeight(int i);
}
