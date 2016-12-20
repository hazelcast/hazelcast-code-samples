package com.hazelcast.samples.spring.data.migration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test English to Spanish translation, using a short list of words
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Active key value repositories with {@code @EnableHazelcastRepositories}</li>
 * <li>Create a test standalone Hazelcast with {@code HazelcastTestInstance} import</li>
 * <li>Create test data manually in {@code @Before} method.</li>
 * </ol>
 */
@ComponentScan
@EnableHazelcastRepositories
@Import(value = HazelcastTestInstance.class)
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = {TranslationServiceTest.class})
public class TranslationServiceTest {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Before
    public void setUp() {
        Noun milk = new Noun();
        milk.setId(1);
        milk.setEnglish("milk");
        milk.setFrench("lait");
        milk.setSpanish("leche");

        IMap<Integer, Noun> nounMap = this.hazelcastInstance.getMap(Noun.class.getCanonicalName());

        nounMap.put(milk.getId(), milk);

        Verb drink = new Verb();
        drink.setId(1);
        drink.setEnglish("drink");
        drink.setFrench("bois");
        drink.setSpanish("bebe");
        drink.setTense(Tense.PRESENT);

        IMap<Integer, Verb> verbMap = this.hazelcastInstance.getMap(Verb.class.getCanonicalName());

        verbMap.put(drink.getId(), drink);
    }

    @Test
    public void en2es() {
        String input = "drink milk";
        String expected = "bebe leche";

        String result = this.translationService.englishToSpanish(input);
        log.info("en2es(), {} -> {}", input, result);

        assertThat(result, equalTo(expected));
    }
}
