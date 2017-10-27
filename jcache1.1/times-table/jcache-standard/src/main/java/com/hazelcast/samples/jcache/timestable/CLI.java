package com.hazelcast.samples.jcache.timestable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
							try {
								this.cacheManager(cacheManager);
							} catch (Exception e) {
								e.printStackTrace(System.out);
							}
							break;

						case CACHENAMES:
							try {
								this.cacheNames(cacheManager);
							} catch (Exception e) {
								e.printStackTrace(System.out);
							}
							break;

						case TIMESTABLE:
							try {
								this.timesTables(cacheManager);
							} catch (Exception e) {
								e.printStackTrace(System.out);
							}
							break;

						case QUIT:
							return;
						}

					} catch (IllegalArgumentException illegalArgumentException) {
						System.out.println("'" + line + "' unrecognised");
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
