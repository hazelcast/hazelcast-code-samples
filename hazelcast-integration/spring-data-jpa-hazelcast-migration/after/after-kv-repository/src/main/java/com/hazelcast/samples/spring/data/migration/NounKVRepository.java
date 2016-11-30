package com.hazelcast.samples.spring.data.migration;

import org.springframework.data.hazelcast.repository.HazelcastRepository;

/**
 * <P>Make a Spring {@link org.springframework.data.hazelcast.repository.HazelcastRepository HazelcastRepository}
 * for Key-Value access to the {@link Noun} data source.
 * </P>
 * <P>Extend the operations provided by the repository with a query method. Spring provides
 * the implementation, no need to code.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add this repository.
 * </LI>
 * </OL>
 */
public interface NounKVRepository extends HazelcastRepository<Noun, Integer> {
	
	public Noun findByEnglish(String s);
	
}
