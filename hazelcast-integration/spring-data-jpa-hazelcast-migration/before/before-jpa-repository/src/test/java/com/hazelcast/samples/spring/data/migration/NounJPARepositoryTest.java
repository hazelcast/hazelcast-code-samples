package com.hazelcast.samples.spring.data.migration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Test CRUD and query operations against a JPA database.
 * </P>
 */
@DataJpaTest
@Slf4j
public class NounJPARepositoryTest extends AbstractNounRepositoryTest {

	@Autowired
	private NounJPARepository nounJPARepository;
	
	@Before
	public void setUp() {
		super.setUp(this.nounJPARepository, log);
	}
		
	@Test
	public void queryEnglish() {
		this.nounJPARepository.save(cat);
		
		Noun cat2 = this.nounJPARepository.findByEnglish("cat");
		log.info("queryEnglish(), read {}", cat2);
		
		assertThat(cat2, not(nullValue()));
		assertThat(cat2, equalTo(cat));
		
		this.nounJPARepository.deleteAll();

		Noun cat3 = this.nounJPARepository.findByEnglish("cat");

		assertThat(cat3, nullValue());
	}
}
