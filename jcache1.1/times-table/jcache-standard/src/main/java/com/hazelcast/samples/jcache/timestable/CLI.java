package com.hazelcast.samples.jcache.timestable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.cache.Cache;
import javax.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Command line processor
 * </p>
 */
@Slf4j
public class CLI {

    public static final String TIMESTABLE_CACHE_NAME = "timestable";

    private enum Command {
        CACHEMANAGER, CACHENAMES, QUIT, TIMES, TIMESTABLE
    }

    /**
     * <p>
     * Process stdin.
     * </p>
     *
     * @param CacheManager
     */
    public void process(CacheManager cacheManager) throws Exception {

        try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {

            this.banner();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.toLowerCase().split(" ");
                if (tokens[0].length() > 0) {
                    try {
                        Command command = Command.valueOf(tokens[0].toUpperCase());
                        System.out.println("> " + command);

                        switch (command) {

                        case CACHEMANAGER:
                                this.cacheManager(cacheManager);
                            break;

                        case CACHENAMES:
                                this.cacheNames(cacheManager);
                            break;

                        case TIMES:
                                 this.times(cacheManager, tokens);
                            break;

                        case TIMESTABLE:
                                this.timesTables(cacheManager);
                            break;

                        case QUIT:
                            return;

                        default:
                        }

                    } catch (Exception exception) {
                            exception.printStackTrace(System.out);
                    }

                    this.banner();
                }
            }
        }
    }


    /**
     * <p>Display the available commands.
     * </p>
     */
    private void banner() {

        System.out.println("===================================================");
        System.out.println(Arrays.asList(Command.values()));
        System.out.println("===================================================");
    }


    /**
     * <p>Show the cache manager implementation.
     * </p>
     *
     * @param cacheManager
     */
    private void cacheManager(CacheManager cacheManager) {
        log.info("-----------------------");
        log.info("CacheManager {}", cacheManager.getClass().getCanonicalName());
        log.info("-----------------------");
    }

    /**
     * <p>Show the caches visible to this cache manager.
     * </p>
     *
     * @param cacheManager
     */
    private void cacheNames(CacheManager cacheManager) {
        log.info("-----------------------");

        Collection<String> cacheNames = new ArrayList<>();
        cacheManager.getCacheNames().forEach(cacheNames::add);

        for (String cacheName : cacheNames) {
            log.info("Cache => name '{}'", cacheName);
        }

        if (cacheNames.size() > 0) {
            log.info("-----------------------");
        }
        log.info("[{} cache{} visible]",
            cacheNames.size(),
            (cacheNames.size() == 1 ? "" : "s")
            );
        log.info("-----------------------");
    }


    /**
     * <p>Multiply the two arguments
     * </p>
     * <p>Usage:
     * <pre>
     * times 5 6
     * </pre> should return "{@code 30}"
     * </p>
     *
     * @param cacheManager
     * @param arg1 Should be a number
     * @param arg2 Should be a number
     */
    private void times(CacheManager cacheManager, String[] tokens) {
        if (tokens.length != 3) {
            log.error("Exactly two arguments required.");
            return;
        }
        int x = Integer.parseInt(tokens[1]);
        int y = Integer.parseInt(tokens[2]);

        if (x <= 0 || y <= 0) {
            log.error("Only positive values allowed.");
            return;
        }

        log.info("-----------------------");
        log.info("Retrieve {} * {}", x, y);

        Instant before = Instant.now();

        // Commutative
        Tuple tuple;
        if (x < y) {
           tuple = new Tuple(x, y);
        } else {
           tuple = new Tuple(y, x);
        }

        int z = x * y;//XXX this.businessLogic.product(tuple);

        Instant after = Instant.now();

        log.info("Result {}", z);
        log.info("-----------------------");
        log.info("Elapsed {}", Duration.between(before, after));
        log.info("-----------------------");
    }

    /**
     * <p>Display the content of the "{@code timestable}"
     * cache.
     * </p>
     *
     * @param cacheManager
     */
    private void timesTables(CacheManager cacheManager) {
        log.info("-----------------------");

        Cache<Tuple, Integer> cache = cacheManager
                .getCache(TIMESTABLE_CACHE_NAME, Tuple.class, Integer.class);

        Map<Tuple, Integer> tmpMap = new TreeMap<>();
        cache.forEach(entry -> tmpMap.put(entry.getKey(), entry.getValue()));

        for (Map.Entry<Tuple, Integer> entry : tmpMap.entrySet()) {
            log.info(" => '{}' == '{}'", entry.getKey(), entry.getValue());
        }

        if (tmpMap.size() > 0) {
            log.info("-----------------------");
        }
        log.info("[{} cache entr{}]",
             tmpMap.size(),
            (tmpMap.size() == 1 ? "y" : "ies")
            );
        log.info("-----------------------");
    }

}
