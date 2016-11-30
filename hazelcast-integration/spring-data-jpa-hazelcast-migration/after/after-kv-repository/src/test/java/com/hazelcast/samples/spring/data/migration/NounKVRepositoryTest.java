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
public class NounKVRepositoryTest extends AbstractNounRepositoryTest {

	@Autowired
	private NounKVRepository nounKVRepository;

	@Before
	public void setUp() {
		super.setUp(this.nounKVRepository, log);
	}
	
	@Test
	public void query() {
		this.nounKVRepository.save(cat);
		
		Noun cat2 = this.nounKVRepository.findByEnglish("cat");
		log.info("query(), read {}", cat2);
		
		assertThat(cat2, not(nullValue()));
		assertThat(cat2, equalTo(cat));
		
		this.nounKVRepository.deleteAll();

		Noun cat3 = this.nounKVRepository.findByEnglish("cat");

		assertThat(cat3, nullValue());
	}
}
