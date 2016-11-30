package com.hazelcast.samples.spring.data.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * <P>Make the translation available to a command line.</P>
 * <P>Usage:</P>
 * <P>
 *  {@code translate --text "hello world"}
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>No changes, continue to use the {@code @Service}.
 * </LI>
 * </OL>
 */
@Component
public class AfterTranslatorCommands implements CommandMarker {

	@Autowired
	private TranslationService translationService;
    
	/**
	 * <P>This is just</P>
	 * <P>{@code public String translate(String input)}</P>
	 * <P>somewhat cluttered by Spring Shell's annotations.
	 * </P>
	 * 
	 * @param input A string of text, hopefully English
	 * @return A string of text, definitely Spanish, hopefully correct
	 */
    @CliCommand(value = "translate", help = "Translate a line of text from English into Spanish")
    public String translate(
    		@CliOption(help="eg translate --text \"hello world\"", mandatory=true, key="text")
    		String input) {
    	try {
    		return this.translationService.englishToSpanish(input);
    	} catch (Exception exception) {
    		return exception.getMessage();
    	}
    }

}
