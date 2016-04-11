package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.spi.properties.GroupProperty;

import java.util.concurrent.TimeUnit;

/**
 * Code sample to demonstrate near-cache behaviour when per entry invalidation is disabled.
 */
public class ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final boolean VERBOSE = Boolean.getBoolean("com.hazelcast.examples.jcache.nearcache.verbose");
    private static final int INVALIDATION_EVENT_FLUSH_FREQ_MSECS =
            1000 * Integer.parseInt(GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue());

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig();
        nearCacheConfig.setInvalidateOnChange(true);

        CacheConfig cacheConfig = createCacheConfig();
        cacheConfig.setDisablePerEntryInvalidationEvents(true);

        ICache<Integer, String> clientCache1 = createCacheWithNearCache(cacheConfig, nearCacheConfig);
        ICache<Integer, String> clientCache2 = getCacheWithNearCache(nearCacheConfig);

        // Put records to cache through client-1.
        putRecordsToCacheOnClient1(clientCache1);

        // Get records from cache through client-2.
        getRecordsFromCacheOnClient2(clientCache2);

        // Get records from near-cache on client-2.
        getRecordsFromNearCacheOnClient2(clientCache2);

        // Update records at cache through client-1.
        updateRecordsInCacheOnClient1(clientCache1);

        // Wait some time and if there are invalidation events to be sent in batch.
        // We assume that they should be flushed, received and processed in this time window already.
        sleep(2 * INVALIDATION_EVENT_FLUSH_FREQ_MSECS);

        // Get old records from near-cache on client-2 because we have disabled per entry invalidation event.
        getStillOldRecordsFromNearCacheOnClient2(clientCache2);

        // Clear cache through client-1.
        clientCache1.clear();

        // Wait some time and if there are invalidation events to be sent in batch.
        // We assume that they should be flushed, received and processed in this time window already.
        sleep(2 * INVALIDATION_EVENT_FLUSH_FREQ_MSECS);

        // Try to get record and can't find any record,
        // since all records are invalidated from near-cache on client-2 due to clear on client-1.
        // Because clear is full-flush operation and it triggers invalidation
        // even though per entry invalidation event is disabled.
        cantFindAnyRecordFromNearCacheOnClient2(clientCache2);

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
            // Anymore, this record is put to also near-cache,
            // at next calls, they will be taken from local near-cache without any remote access.
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
                    : "Taken value from near-cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from near-cache on client-2");
            }
            // Since this record has been put to near-cache at previous,
            // it is taken from near-cache without any remote access.
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from near-cache finished in " + elapsed + " milliseconds");
    }

    private void updateRecordsInCacheOnClient1(ICache<Integer, String> clientCache1) {
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(RECORD_COUNT + i);
            clientCache1.put(i, value);
            if (VERBOSE) {
                System.out.println("Update key=" + i + ", value=" + value + " to cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been updated in cache through client-1");
    }

    private void getStillOldRecordsFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from near-cache must not be updated value " + actualValue
                    + " but old value " + expectedValue
                    + ". Because it must not be invalidated disabled per entry invalidation.";
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", old value=" + actualValue
                        + " from near-cache through client-2 after update");
            }
            // This record has not been invalidated at near-cache on client-2.
            // Client-1 has updated the record and since per entry invalidation event is disabled,
            // invalidation events are not sent to client-2.
            // So this record has still its old value.
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get old records from near-cache after update finished in " + elapsed + " milliseconds");
    }

    private void cantFindAnyRecordFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = clientCache2.get(i);
            assert value == null
                    : "Taken value from near-cache must not be there but it is " + value
                    + ". Because it must be invalidated due to clear on cache!";
            if (VERBOSE && value != null) {
                System.out.println("Get key=" + i + ", value=" + value
                        + " from near-cache through client-2 after clear");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from near-cache after clear finished in " + elapsed + " milliseconds");
    }

    public static void main(String[] args) {
        ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled clientNearCacheUsage
                = new ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled();
        clientNearCacheUsage.run();
    }

}
