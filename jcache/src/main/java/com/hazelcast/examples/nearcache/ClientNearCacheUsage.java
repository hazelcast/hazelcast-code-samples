package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.spi.properties.GroupProperty;

import java.util.concurrent.TimeUnit;

public class ClientNearCacheUsage extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final int BATCH_FREQUENCY_MILISECONDS = Integer.parseInt(GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue()) * 1000;
    private static final boolean VERBOSE = Boolean.getBoolean("com.hazelcast.examples.jcache.nearcache.verbose");

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig();
        nearCacheConfig.setInvalidateOnChange(true);

        ICache<Integer, String> clientCache1 = createCacheWithNearCache(nearCacheConfig);
        ICache<Integer, String> clientCache2 = getCacheWithNearCache();

        // put records to cache through client-1
        putRecordsToCacheOnClient1(clientCache1);

        // gets records from cache through client-2
        getRecordsFromCacheOnClient2(clientCache2);

        // gets records from near-cache on client-2
        getRecordsFromNearCacheOnClient2(clientCache2);

        // update records at cache through client-1
        updateRecordsInCacheOnClient1(clientCache1);

        // wait a little for invalidation events
        sleep(BATCH_FREQUENCY_MILISECONDS + 5000);

        // gets invalidated records from near-cache on client-2
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
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue) : "Taken value from cache must be " + expectedValue
                    + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from cache through client-2");
            }
            // anymore, this record is put to also near-cache,
            // at next calls, they will be taken from local near-cache without any remote access
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from cache finished in " + elapsed + " milliseconds");
    }

    private void getRecordsFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from near-cache on client-2");
            }
            // since this record has been put to near-cache at previous,
            // it is taken from near-cache without any remote access.
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from near-cache finished in " + elapsed + " milliseconds");
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
            // this record has been invalidated at near-cache on client-2
            // since client-1 has updated the record and invalidation events are sent to client-2,
            // so this record has been taken from cache (not near-cache) through client-2
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get invalidated records from near-cache finished in " + elapsed + " milliseconds");
    }

    public static void main(String[] args) {
        ClientNearCacheUsage clientNearCacheUsage = new ClientNearCacheUsage();
        clientNearCacheUsage.run();
    }
}
