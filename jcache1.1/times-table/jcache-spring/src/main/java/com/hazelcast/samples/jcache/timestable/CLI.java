package com.hazelcast.samples.jcache.timestable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Commands additional to the defaults provided
 * by Spring Shell.
 * </p>
 */
@Component
@Slf4j
public class CLI implements CommandMarker {

    public static final String TIMESTABLE_CACHE_NAME = "timestable";

    @Autowired
    private BusinessLogic businessLogic;
    @Autowired
    private CacheManager cacheManager;

    /**
     * <p>Display the cache manager.
     * </p>
     */
    @CliCommand(value = "cacheManager",
            help = "Display the cache manager")
    public void cacheManager() {
        log.info("-----------------------");
        log.info("CacheManager {}", this.cacheManager.getClass().getCanonicalName());
        log.info("-----------------------");
    }

    /**
     * <p>Display the caches known to this cache manager,
     * those that have been accessed by this client.
     * </p>
     */
    @CliCommand(value = "cacheNames",
            help = "Display the cache known to this cache manager")
    public void cacheNames() {
        log.info("-----------------------");

        Collection<String> cacheNames = new ArrayList<>();
        this.cacheManager.getCacheNames().forEach(cacheNames::add);

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
     * <p>Retrieve the times table entry for "{@code x * y}",
     * creating and caching the result where necessary.
     * </p>
     * <p>Usage:
     * <pre>
     * times --x 5 --y 6
     * </pre> should return "{@code 30}"
     * </p>
     *
     * @param x Should be greater than 0
     * @param y Should be greater than 0
     */
    @CliCommand(value = "times",
        help = "Display the times table for (x,y)")
    public void times(
        @CliOption(key = {"x"},
                mandatory = true,
                help = "The left operand")
        int x,
        @CliOption(key = {"y"},
                mandatory = true,
                help = "The right operand")
        int y
        ) {

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

        int z = this.businessLogic.product(tuple);

        Instant after = Instant.now();

        log.info("Result {}", z);
        log.info("-----------------------");
        log.info("Elapsed {}", Duration.between(before, after));
        log.info("-----------------------");
    }


    /**
     * <p>Dump the content of the 'timestable' cache.
     * </p>
     */
    @CliCommand(value = "timesTable",
            help = "Display the content of the 'timestable' cache")
    public void timestable() {
        log.info("-----------------------");

        Cache<Tuple, Integer> cache = this.cacheManager
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
