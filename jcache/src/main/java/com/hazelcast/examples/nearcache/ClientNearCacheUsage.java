package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.NearCacheConfig;

public class ClientNearCacheUsage extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final boolean VERBOSE = Boolean.getBoolean("com.hazelcast.examples.jcache.nearcache.verbose");

    public void run() {
        long start;

        NearCacheConfig nearCacheConfig = createNearCacheConfig();
        nearCacheConfig.setInvalidateOnChange(true);

        ICache<Integer, String> clientCache1 = createCacheWithNearCache();
        ICache<Integer, String> clientCache2 = createCacheWithNearCache();



        // Put records to cache through client-1
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i);
            clientCache1.put(i, value);
            if (VERBOSE) {
                System.out.println("Put key=" + i + ", value=" + value + " to cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been put to cache through client-1");



        start = System.nanoTime();
        // Gets records from cache through client-2
        for (int i = 0; i < RECORD_COUNT; i++) {
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue) :
                    "Taken value from cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from cache through client-2");
            }
            // Anymore, this record is put to also near-cache,
            // at next calls, they will be taken from local near-cache without any remote access
        }
        System.out.println("Get records from cache finished in "
                + ((System.nanoTime() - start) / 1000000) + " milliseconds");



        start = System.nanoTime();
        // Gets records from near-cache on client-2
        for (int i = 0; i < RECORD_COUNT; i++) {
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue) :
                    "Taken value from cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from near-cache on client-2");
            }
            // Since this record has been put to near-cache at previous,
            // it is taken from near-cache without any remote access.
        }
        System.out.println("Get records from near-cache finished in "
                + ((System.nanoTime() - start) / 1000000) + " milliseconds");



        // Update records at cache through client-1
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i * i);
            clientCache1.put(i, value);
            if (VERBOSE) {
                System.out.println("Update key=" + i + ", value=" + value + " to cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been updated in cache through client-1");



        try {
            // Wait a little for invalidation events
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        start = System.nanoTime();
        // Gets invalidated records from near-cache on client-2
        for (int i = 0; i < RECORD_COUNT; i++) {
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            try {
                assert actualValue.equals(expectedValue) :
                        "Taken value from cache should be " + expectedValue + " but it is " + actualValue;
            } catch (AssertionError assertionError) {
                System.out.println("Seems that invalidation event for record with key " + i + " has not reached yet");
            }
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from cache through client-2 after invalidation");
            }
            // This record has been invalidated at near-cache on client-2
            // since client-1 has updated the record and invalidation events are sent to client-2.
            // So this record has been taken from cache (not near-cache) through client-2.
        }
        System.out.println("Get invalidated records from near-cache finished in "
                + ((System.nanoTime() - start) / 1000000) + " milliseconds");



        shutdown();
    }

    public static void main(String[] args) {
        ClientNearCacheUsage clientNearCacheUsage = new ClientNearCacheUsage();
        clientNearCacheUsage.run();
    }

}
