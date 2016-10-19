package com.hazelcast.samples.spring.data.chemistry.repository;

import com.hazelcast.samples.spring.data.chemistry.domain.Element;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

import java.util.List;

/**
 * An {@code ElementRepository} subtypes
 * {@link org.springframework.data.keyvalue.repository.KeyValueRepository}
 * providing its features for the {@link Element} domain model.
 *
 * It would be equally valid for this to extend
 * {@link org.springframework.data.hazelcast.repository.HazelcastRepository}
 * to more strongly associate the repository with the implementation.
 *
 * Define an additional method {@code findByGroup}, which is implemented at runtime by Spring
 * as it can deduce that it is mean a search where {@code element.getGroup()} matches the
 * provided argument.
 */
public interface ElementRepository extends KeyValueRepository<Element, String> {

    List<Element> findByGroupOrderBySymbolDesc(Integer group);
}
