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
 * <LI>The {@code findByEnglish()} query method has been removed from {@code NounJPARepository}
 * so remove its test.
 * </LI>
 * <LI>Add a test for the {@code findAllId()} query method that has been added.
 * </LI>
 * </OL>
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
	public void queryIds() {
		this.nounJPARepository.save(cat);
		
		Iterator<Integer> iterator = this.nounJPARepository.findAllId().iterator();

		long count = 0;
		
		while (iterator.hasNext()) {
			count++;
			iterator.next();
		}
		
		log.info("queryIds(), counted {}", count);
		
		assertThat("Iterator count", count, equalTo(1L));
		assertThat("Repository count", count, equalTo(this.nounJPARepository.count()));
		
		this.nounJPARepository.deleteAll();
		assertThat("Repository clear", this.nounJPARepository.count(), equalTo(0L));
	}

}
