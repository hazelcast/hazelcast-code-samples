package com.hazelcast.samples.spring.data.migration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Test CRUD and query operations against Hazelcast.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add this test class.
 * </LI>
 * </OL>
 */
@Configuration
@EnableHazelcastRepositories
@Import(value=HazelcastTestInstance.class)
@Slf4j
public class VerbKVRepositoryTest extends AbstractVerbRepositoryTest {

	@Autowired
    private VerbKVRepository verbKVRepository;

	@Before
	public void setUp() {
		super.setUp(this.verbKVRepository, log);
	}
	
	@Test
	public void query() {
		this.verbKVRepository.save(invite);
		
		Verb invite2 = this.verbKVRepository.findByEnglish("invite");
		log.info("query(), read {}", invite2);
		
		assertThat(invite2, not(nullValue()));
		assertThat(invite2, equalTo(invite));
		
		this.verbKVRepository.deleteAll();

		Verb invite3 = this.verbKVRepository.findByEnglish("invite");

		assertThat(invite3, nullValue());
	}
}
