package com.hazelcast.samples.jcache.timestable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;

/**
 * <p>Common utilities for all flavours of clients.
 * </p>
 */
public class Util {
    private static final String PROMPT_PREFIX = "jsr107=";
    private static final String PROPERTIES_FILE_NAME = "my.properties";

    private static String prompt;

    /**
     * <p>JCache configuration for the "{@code timestable}" cache.
     * The key is {@link Tuple} holding the input to the times
     * table argument (eg. the pair {@code 5} and {@code 6}) and
     * the value is the resulting number.
     * </p>
     * <p>Add a cache listener so we can observe when items are
     * actually added to the cache, as this helps to prove when
     * caching is happening and when it is not.
     * </p>
     *
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static MutableConfiguration timesTableConfiguration() {
        MutableConfiguration<Tuple, Integer> mutableConfiguration = new MutableConfiguration<>();

        mutableConfiguration.setTypes(Tuple.class, Integer.class);

        CacheEntryListenerConfiguration cacheEntryListenerConfiguration
        = new MutableCacheEntryListenerConfiguration(
                // Factory, FilterFactory, is old value required?, is synchronous?
                new MyCacheListenerFactory(), null, false, false);

        mutableConfiguration.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);

        return mutableConfiguration;
    }


    /**
     * <P>Read the "{@code my.properties}" file (once) to determine
     * the version of JCache being used, for prompting at the
     * command line.
     * </p>
     *
     * @return {@code 1.0.0} or {@code 1.1.0}
     */
    public static synchronized String getPrompt() {
        if (prompt != null) {
            return prompt;
        }

        try (InputStream inputStream =
                Util.class.getClassLoader().getResourceAsStream("/" + PROPERTIES_FILE_NAME);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);)
        {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(PROMPT_PREFIX)) {
                    String value = line.substring(PROMPT_PREFIX.length()).trim();
                    prompt = PROMPT_PREFIX + value + " > ";
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace(System.err);
        }

        if (prompt == null) {
            prompt = "Error reading '" + PROPERTIES_FILE_NAME + "' > ";
        }

        return prompt;
    }
}
