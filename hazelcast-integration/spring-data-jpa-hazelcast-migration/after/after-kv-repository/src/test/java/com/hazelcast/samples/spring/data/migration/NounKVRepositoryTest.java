package com.hazelcast.samples.spring.data.migration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test CRUD and query operations against Hazelcast.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add this test class.</li>
 * </ol>
 */
@Configuration
@EnableHazelcastRepositories
@Import(value = HazelcastTestInstance.class)
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
