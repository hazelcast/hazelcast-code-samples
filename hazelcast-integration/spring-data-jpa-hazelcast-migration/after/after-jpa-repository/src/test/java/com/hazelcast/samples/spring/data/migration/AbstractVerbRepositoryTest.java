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
 * Test CRUD operations against a Spring repository for {@link Verb}
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>No changes required.</li>
 * </ol>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Object.class})
public abstract class AbstractVerbRepositoryTest {

    protected static Verb invite;

    private Logger log;
    private CrudRepository<Verb, Integer> verbRepository;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        invite = new Verb();
        invite.setId(1);
        invite.setEnglish("invite");
        invite.setFrench("inviter");
        invite.setSpanish("invitar");
        invite.setTense(Tense.PRESENT);
    }

    public void setUp(CrudRepository<Verb, Integer> arg0, Logger arg1) {
        this.verbRepository = arg0;
        this.log = arg1;
    }

    @Test
    public void curd() {
        assertThat("Empty before", this.verbRepository.count(), equalTo(0L));

        this.verbRepository.save(invite);

        assertThat("Not empty during", this.verbRepository.count(), equalTo(1L));

        Verb invite2 = this.verbRepository.findOne(invite.getId());
        this.log.info("curd(), read {}", invite2);

        assertThat(invite2, not(nullValue()));
        assertThat("System.identityHashCode", System.identityHashCode(invite2), not(equalTo(System.identityHashCode(invite))));
        assertThat(invite2, equalTo(invite));

        this.verbRepository.delete(invite.getId());

        assertThat("Empty after", this.verbRepository.count(), equalTo(0L));
    }
}
