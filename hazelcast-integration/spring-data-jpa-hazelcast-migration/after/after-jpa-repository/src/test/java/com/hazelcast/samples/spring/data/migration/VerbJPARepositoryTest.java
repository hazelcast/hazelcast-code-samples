package com.hazelcast.samples.spring.data.migration;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Test CRUD operations against a JPA database.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>The {@code findByEnglish()} query method has been removed from {@code VerbJPARepository}
 * so remove its test.
 * </LI>
 * <LI>Add a test for the {@code findAllId()} query method that has been added.
 * </LI>
 * </OL>
 */
@DataJpaTest
@Slf4j
public class VerbJPARepositoryTest extends AbstractVerbRepositoryTest {

	@Autowired
	private VerbJPARepository verbJPARepository;
	
	@Before
	public void setUp() {
		super.setUp(this.verbJPARepository, log);
	}

	@Test
	public void queryIds() {
		this.verbJPARepository.save(invite);
		
		Iterator<Integer> iterator = this.verbJPARepository.findAllId().iterator();

		long count = 0;
		
		while (iterator.hasNext()) {
			count++;
			iterator.next();
		}
		
		log.info("queryIds(), counted {}", count);
		
		assertThat("Iterator count", count, equalTo(1L));
		assertThat("Repository count", count, equalTo(this.verbJPARepository.count()));
		
		this.verbJPARepository.deleteAll();
		assertThat("Repository clear", this.verbJPARepository.count(), equalTo(0L));
	}

}
