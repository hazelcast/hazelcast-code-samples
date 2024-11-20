package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Make a Spring {@link org.springframework.data.repository.CrudRepository CrudRepository}
 * for JPA access to the {@link Verb} data source.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Remove a querying method, don't query JPA by English column.</li>
 * <li>Add a querying method, Hazelcast finds it useful to know all Id values.</li>
 * </ol>
 */
public interface VerbJPARepository extends CrudRepository<Verb, Integer> {

    @Query("SELECT v.id FROM Verb v")
    Iterable<Integer> findAllId();
}
