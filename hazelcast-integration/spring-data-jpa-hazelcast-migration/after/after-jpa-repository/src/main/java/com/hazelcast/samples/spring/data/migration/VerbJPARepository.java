package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * <P>Make a Spring {@link org.springframework.data.repository.CrudRepository CrudRepository}
 * for JPA access to the {@link Verb} data source.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Remove a querying method, don't query JPA by English column
 * </LI>
 * <LI>Add a querying method, Hazelcast finds it useful to know all Id values.
 * </LI>
 * </OL>
 */
public interface VerbJPARepository extends CrudRepository<Verb, Integer> {

	@Query("SELECT v.id FROM Verb v")
    public Iterable<Integer> findAllId();

}
