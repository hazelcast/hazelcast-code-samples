package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.repository.CrudRepository;

/**
 * Make a Spring {@link org.springframework.data.repository.CrudRepository CrudRepository}
 * for JPA access to the {@link Noun} data source.
 *
 * Extend the provided operations of {@code CrudRepository} with a query.
 * Spring derives the query implementation from the method name, no need to code.
 */
public interface NounJPARepository extends CrudRepository<Noun, Integer> {

    Noun findByEnglish(String s);
}
