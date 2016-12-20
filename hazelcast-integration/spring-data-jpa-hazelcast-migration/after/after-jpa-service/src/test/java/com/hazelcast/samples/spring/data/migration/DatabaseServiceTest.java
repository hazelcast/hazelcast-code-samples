package com.hazelcast.samples.spring.data.migration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the {@link DatabaseService}, that provides database operations to Hazelcast.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add this test class.</li>
 * </ol>
 */
@ComponentScan
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = {DatabaseServiceTest.class})
@Sql("classpath:testdata.sql")
public class DatabaseServiceTest {

    @Autowired
    private DatabaseService databaseService;

    @Test
    public void findNoun() {
        Noun noun = this.databaseService.findNoun(1);

        log.info("findNoun(), {}", noun);

        assertThat(noun, not(nullValue()));
    }

    @Test
    public void findVerb() {
        Verb verb = this.databaseService.findVerb(9);

        log.info("findVerb(), {}", verb);

        assertThat(verb, not(nullValue()));
    }

    @Test
    public void findNounIds() {
        Iterator<Integer> iterator = this.databaseService.findNounIds().iterator();

        Set<Integer> results = new TreeSet<>();
        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        assertThat(results, hasItems(1, 2));
    }

    @Test
    public void findVerbIds() {
        Iterator<Integer> iterator = this.databaseService.findVerbIds().iterator();

        Set<Integer> results = new TreeSet<>();
        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        assertThat(results, hasItems(9, 10));
    }
}
