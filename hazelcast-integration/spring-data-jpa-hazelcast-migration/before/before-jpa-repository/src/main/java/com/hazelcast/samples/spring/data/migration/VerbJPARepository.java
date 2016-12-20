package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.repository.CrudRepository;

/**
 * Make a Spring {@link org.springframework.data.repository.CrudRepository CrudRepository}
 * for JPA access to the {@link Verb} data source.
 *
 * Extend the provided operations of {@code CrudRepository} with a query.
 * Spring derives the query implementation from the method name, no need to code.
 */
public interface VerbJPARepository extends CrudRepository<Verb, Integer> {

    Verb findByEnglish(String s);
}
