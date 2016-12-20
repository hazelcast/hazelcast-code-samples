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
