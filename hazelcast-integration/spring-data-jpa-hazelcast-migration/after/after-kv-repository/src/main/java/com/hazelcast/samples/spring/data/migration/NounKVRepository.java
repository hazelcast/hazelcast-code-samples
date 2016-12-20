package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.hazelcast.repository.HazelcastRepository;

/**
 * Make a Spring {@link org.springframework.data.hazelcast.repository.HazelcastRepository HazelcastRepository}
 * for Key-Value access to the {@link Noun} data source.
 *
 * Extend the operations provided by the repository with a query method.
 * Spring provides the implementation, no need to code.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add this repository.</li>
 * </ol>
 */
public interface NounKVRepository extends HazelcastRepository<Noun, Integer> {

    Noun findByEnglish(String s);
}
