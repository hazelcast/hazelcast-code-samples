package com.hazelcast.samples.jcache.timestable;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The application entry point.
 */
@Slf4j
public class Application {

    /**
     * Set Hazelcast logging type and JCache control invariants.
     * <p>
     * The caching provider will build a Hazelcast client using
     * the default configuration file "{@code hazelcast-client.xml}".
     *
     * @param args From command line
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.jcache.provider.type", "client");
        System.setProperty("hazelcast.logging.type", "slf4j");

        /* Create the cache manager, which will build a
         * Hazelcast client.
         */
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        /* Command line processing
         */
        CLI cli = new CLI();
        cli.process(cacheManager, Util.getPrompt());

        /* JVM shutdown hooks should close the cache and client,
         * but do so manually to show how it could be done.
         */
        if (!cacheManager.isClosed()) {
            log.info("cacheManager.close()");
            cacheManager.close();

            /* JCache 1.1 specifies an exception should be
             * thrown trying to access cache names after
             * cache closure.
             */
            try {
                Collection<String> cacheNames = new ArrayList<>();
                cacheManager.getCacheNames().forEach(cacheNames::add);

                log.error("JCache 1.0 behaviour, {}", cacheNames);
            } catch (IllegalStateException jcache11Exception) {
                log.info("JCache1.1 behaviour, {}", jcache11Exception.getLocalizedMessage());
            }

            // Close Hazelcast connection
            HazelcastCacheManager hazelcastCacheManager = (HazelcastCacheManager) cacheManager;
            HazelcastInstance hazelcastInstance = hazelcastCacheManager.getHazelcastInstance();

            if (hazelcastInstance.getLifecycleService().isRunning()) {
                hazelcastInstance.shutdown();
            }
        }
        System.exit(0);
    }
}
