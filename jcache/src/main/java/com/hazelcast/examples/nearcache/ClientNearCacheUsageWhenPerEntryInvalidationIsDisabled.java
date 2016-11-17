package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.NearCacheConfig;

import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;
import static com.hazelcast.spi.properties.GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS;
import static java.lang.Integer.parseInt;

/**
 * Code sample to demonstrate Near Cache behaviour when per entry invalidation is disabled.
 */
public class ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final boolean VERBOSE = Boolean.getBoolean("com.hazelcast.examples.jcache.nearcache.verbose");
    private static final int INVALIDATION_EVENT_FLUSH_FREQ_SECONDS
            = parseInt(CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue());

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInvalidateOnChange(true);

        CacheConfig<Integer, String> cacheConfig = createCacheConfig();
        cacheConfig.setDisablePerEntryInvalidationEvents(true);

        ICache<Integer, String> clientCache1 = createCacheWithNearCache(cacheConfig, nearCacheConfig);
        ICache<Integer, String> clientCache2 = getCacheWithNearCache(nearCacheConfig);

        // put records to remote cache through client-1
        putRecordsToCacheOnClient1(clientCache1);

        // get records from remote cache through client-2
        getRecordsFromCacheOnClient2(clientCache2);

        // get records from Near Cache on client-2
        getRecordsFromNearCacheOnClient2(clientCache2);

        // update records in remote cache through client-1
        updateRecordsInCacheOnClient1(clientCache1);

        // wait a little for invalidation events to be sent in batch
        sleepSeconds(2 * INVALIDATION_EVENT_FLUSH_FREQ_SECONDS);

        // get old records from Near Cache on client-2 (because we have disabled per entry invalidation event)
        getStillOldRecordsFromNearCacheOnClient2(clientCache2);

        // clear cache through client-1
        clientCache1.clear();

        // wait a little for invalidation events to be sent in batch
        sleepSeconds(2 * INVALIDATION_EVENT_FLUSH_FREQ_SECONDS);

        // try to get records from Near Cache and can't find any, since they are invalidated from Near Cache on client-2
        // due to clear() on client-1, because it's a full-flush operation and triggers invalidation even though
        // per entry invalidation event is disabled
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
                    : "Taken value from Near Cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from Near Cache on client-2");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from Near Cache finished in " + elapsed + " milliseconds");
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
            // these records have not been invalidated in the Near Cache on client-2,
            // because client-1 has updated the records and per entry invalidation event is disabled,
            // so invalidation events are not sent to client-2 (so this record has still its old value)
            String actualValue = clientCache2.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from Near Cache must not be updated value " + actualValue + " but old value " + expectedValue
                    + ". Because it must not be invalidated disabled per entry invalidation.";
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", old value=" + actualValue
                        + " from Near Cache through client-2 after update");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get old records from Near Cache after update finished in " + elapsed + " milliseconds");
    }

    private void cantFindAnyRecordFromNearCacheOnClient2(ICache<Integer, String> clientCache2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = clientCache2.get(i);
            assert value == null
                    : "Taken value from Near Cache must not be there but it is " + value
                    + ". Because it must be invalidated due to clear on cache!";
            if (VERBOSE && value != null) {
                System.out.println("Get key=" + i + ", value=" + value + " from Near Cache through client-2 after clear()");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from Near Cache after clear() finished in " + elapsed + " milliseconds");
    }

    public static void main(String[] args) {
        ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled clientNearCacheUsage
                = new ClientNearCacheUsageWhenPerEntryInvalidationIsDisabled();
        clientNearCacheUsage.run();
    }
}
