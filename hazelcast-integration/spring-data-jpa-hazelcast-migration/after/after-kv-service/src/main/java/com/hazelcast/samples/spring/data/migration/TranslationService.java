package com.hazelcast.samples.spring.data.migration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <P>A translation service that won't win prizes, or not the kind
 * of prizes you'd want.
 * </P>
 * <P>Only English to Spanish is implemented.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Replace {@link NounJPARepository} with {@link NounKVRepository}
 * </LI>
 * <LI>Replace {@link VerbJPARepository} with {@link VerbKVRepository}
 * </LI>
 * </OL>
 */
@Service
public class TranslationService {
	private static final String UNKNOWN = "?";

	@Autowired
	private NounKVRepository nounKVRepository;
	
	@Autowired
	private VerbKVRepository verbKVRepository;
	
	/**
	 * <P>Simplistic translation, not catering for plurals, tenses,
	 * accents, grammar, etc. For the input string, look for matching
	 * words in the database and replace them.
	 * </P>
	 * 
	 * @param input One or more English words, separated by spaces
	 * @return Spanish words
	 */
	public String englishToSpanish(String input) {
		if (input==null) {
			return "";
		}
		
		// Make an array of words, lower case letters only
		String[] words = input.toLowerCase().replaceAll("[^a-z ]", "").trim().split(" ");
		
		for (int i=0; i<words.length; i++) {
			Noun noun = this.nounKVRepository.findByEnglish(words[i]);
			if (noun!=null) {
				words[i] = noun.getSpanish();
			} else {
				Verb verb = this.verbKVRepository.findByEnglish(words[i]);
				if (verb!=null) {
					words[i] = verb.getSpanish();
				} else {
					words[i] = UNKNOWN;
				}
			}
		}
		
		return Arrays.asList(words).toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll(",","");
	}
}
