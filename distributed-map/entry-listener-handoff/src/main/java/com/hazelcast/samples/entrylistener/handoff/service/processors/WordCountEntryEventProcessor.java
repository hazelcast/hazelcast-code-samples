package com.hazelcast.samples.entrylistener.handoff.service.processors;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.core.EntryEvent;

/**
 * A Simple Callable that takes the value of the EntryEvent which is a String
 * and then splits it by a space delimiter.  After which the number of words is counted
 * and returned as the result.
 */
public class WordCountEntryEventProcessor
        implements EntryEventProcessor<Integer,String,String> {

    @Override
    public String process(EntryEvent<Integer, String> entryEvent) throws EntryEventServiceException {
        String value = entryEvent.getValue();
        //String[] words = value.split(" ");
        int words = countWords(value);
        return entryEvent.getKey() + " has " + Integer.valueOf(words);
    }

    private int countWords(String s){

        int wordCount = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is a letter, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                // if char isn't a letter and there have been letters before,
                // counter goes up.
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                // last word of String; if it doesn't end with a non letter, it
                // wouldn't count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }
}
