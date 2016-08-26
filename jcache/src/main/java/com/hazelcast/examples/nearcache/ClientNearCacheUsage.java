package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.NearCacheConfig;

import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.getBoolean;

public class ClientNearCacheUsage extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final boolean VERBOSE = getBoolean("com.hazelcast.examples.jcache.nearcache.verbose");

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig();
        nearCacheConfig.setInvalidateOnChange(true);

        ICache<Integer, String> clientCache1 = createCacheWithNearCache(nearCacheConfig);
        ICache<Integer, String> clientCache2 = getCacheWithNearCache();

        // put records to remote cache through client-1
        putRecordsToCacheOnClient1(clientCache1);

        // get records from remote cache through client-2
        getRecordsFromCacheOnClient2(clientCache2);

        // get records from Near Cache on client-2
        getRecordsFromNearCacheOnClient2(clientCache2);

        // update records in remote cache through client-1
        updateRecordsInCacheOnClient1(clientCache1);

        // wait a little for invalidation events
        waitForInvalidationEvents();

        // get invalidated records from remote cache on client-2
        getInvalidatedRecordsFromNearCacheOnClient2(clientCache2);

        shutdown();
    }

    private void putRecordsToCacheOnClient1(ICache<Integer, String> clientCache1) {
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i);
            clientCache1.put(i, value);
            if (VERBOSE) {
                System.out.println("Put key=" + i + ", value=" + value + " to cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been put to cache through client-1");
    }

    private void getRecordsFromCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // these get() calls populate the Near Cache, so at the next calls,
            // the values will be taken from local Near Cache without any remote access
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue) : "Taken value from cache must be " + expectedValue
                    + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from cache through client-2");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from cache finished in " + elapsed + " milliseconds");
    }

    private void getRecordsFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // since this record has been put to Near Cache before,
            // it is taken from the local Near Cache without any remote access
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from Near Cache on client-2");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from Near Cache finished in " + elapsed + " milliseconds");
    }

    private void updateRecordsInCacheOnClient1(ICache<Integer, String> clientCache1) {
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i * i);
            clientCache1.put(i, value);
            if (VERBOSE) {
                System.out.println("Update key=" + i + ", value=" + value + " to cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been updated in cache through client-1");
    }

    private void getInvalidatedRecordsFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // these record have been invalidated at Near Cache on client-2,
            // because client-1 has updated the records and invalidation events are sent to client-2,
            // so the records have been taken from the remote cache (not the local Near Cache) through client-2
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            try {
                assert actualValue.equals(expectedValue)
                        : "Taken value from cache should be " + expectedValue + " but it is " + actualValue;
            } catch (AssertionError assertionError) {
                System.out.println("Seems that invalidation event for record with key " + i + " has not reached yet");
            }
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from cache through client-2 after invalidation");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get invalidated records from Near Cache finished in " + elapsed + " milliseconds");
    }

    public static void main(String[] args) {
        ClientNearCacheUsage clientNearCacheUsage = new ClientNearCacheUsage();
        clientNearCacheUsage.run();
    }
}
