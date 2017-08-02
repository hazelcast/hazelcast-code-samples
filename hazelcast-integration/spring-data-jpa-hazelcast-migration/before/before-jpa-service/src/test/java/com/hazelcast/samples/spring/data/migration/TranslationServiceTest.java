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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * <P>Test English to Spanish translation, using a short list of words
 * </P>
 */
@ComponentScan
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes={TranslationServiceTest.class})
@Sql("classpath:testdata.sql")
public class TranslationServiceTest {
		
	@Autowired
	private TranslationService translationService;
	
	@Test
	public void en2es() {
		String input = "drink milk";
		String expected = "bebe leche";
		
		String result = this.translationService.englishToSpanish(input);
		log.info("en2es(), {} -> {}", input, result);
		
		assertThat(result, equalTo(expected));
	}
}
