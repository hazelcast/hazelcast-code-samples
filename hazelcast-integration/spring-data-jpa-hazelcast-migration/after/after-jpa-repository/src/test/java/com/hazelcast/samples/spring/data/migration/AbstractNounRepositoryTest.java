package com.hazelcast.samples.spring.data.migration;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test CRUD operations against a Spring repository for {@link Noun}
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>No changes required.</li>
 * </ol>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Object.class})
public abstract class AbstractNounRepositoryTest {

    protected static Noun cat;

    private Logger log;
    private CrudRepository<Noun, Integer> nounRepository;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        cat = new Noun();
        cat.setId(1);
        cat.setEnglish("cat");
        cat.setFrench("chat");
        cat.setSpanish("gato");
    }

    public void setUp(CrudRepository<Noun, Integer> arg0, Logger arg1) {
        this.nounRepository = arg0;
        this.log = arg1;
    }

    @Test
    public void curd() {
        assertThat("Empty before", this.nounRepository.count(), equalTo(0L));

        this.nounRepository.save(cat);

        assertThat("Not empty during", this.nounRepository.count(), equalTo(1L));

        Noun cat2 = this.nounRepository.findOne(cat.getId());
        this.log.info("curd(), read {}", cat2);

        assertThat(cat2, not(nullValue()));
        assertThat("System.identityHashCode", System.identityHashCode(cat2), not(equalTo(System.identityHashCode(cat))));
        assertThat(cat2, equalTo(cat));

        this.nounRepository.delete(cat.getId());

        assertThat("Empty after", this.nounRepository.count(), equalTo(0L));
    }
}
