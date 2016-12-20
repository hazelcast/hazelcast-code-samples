package com.hazelcast.samples.spring.data.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * Make the translation available to a command line.
 *
 * Usage:
 * {@code translate --text "hello world"}
 */
@Component
public class BeforeTranslatorCommands implements CommandMarker {

    @Autowired
    private TranslationService translationService;

    /**
     * This is just {@code public String translate(String input)} somewhat cluttered by Spring Shell's annotations.
     *
     * @param input A string of text, hopefully English
     * @return A string of text, definitely Spanish, hopefully correct
     */
    @CliCommand(value = "translate", help = "Translate a line of text from English into Spanish")
    public String translate(
            @CliOption(help = "eg translate --text \"hello world\"", mandatory = true, key = "text")
                    String input) {
        try {
            return this.translationService.englishToSpanish(input);
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }
}
