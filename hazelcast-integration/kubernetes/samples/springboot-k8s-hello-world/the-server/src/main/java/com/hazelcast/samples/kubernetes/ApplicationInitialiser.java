package com.hazelcast.samples.kubernetes;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * <p>
 * Ensure the cluster has some test data to work with, by loading
 * five strings into a map if it empty.
 * </p>
 * <p>
 * If the map is empty, we are the first server to start, so load
 * the data. If the map isn't empty, we're not the first server
 * so don't need to bother. So the goal is to initialise the map
 * once.
 * </p>
 * <p>The coding here is simple, so there is a race condition that
 * if the first two servers start at exactly the same time, both
 * could find the map empty and both do the load. This is harmless
 * in this example so we don't bother with any clever solutions.
 * </p>
 */
@Component
public class ApplicationInitialiser implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitialiser.class);

    private static final String[][] GREETINGS = new String[][] {
        { "english", "hello world" },
        { "espanol", "hola mundo" },
        { "deutsch", "hallo welt" },
        { "italiano", "ciao mondo" },
        { "francais", "bonjour le monde" },
    };

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void run(String... arg0) throws Exception {
        IMap<String, String> helloMap = this.hazelcastInstance.getMap("hello");

        if (!helloMap.isEmpty()) {
            LOGGER.info("Skip loading '{}', not empty", helloMap.getName());
        } else {
            Arrays.stream(GREETINGS).forEach(pair -> {
                helloMap.set(pair[0], pair[1]);
            });
            LOGGER.info("Loaded {} into '{}'", GREETINGS.length, helloMap.getName());
        }
    }

}
