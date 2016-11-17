package nearcache;

import com.hazelcast.config.NearCacheConfig;

import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class ClientHiDensityNearCacheUsage extends ClientHiDensityNearCacheUsageSupport {

    private static final int RECORD_COUNT = 1000;
    private static final boolean VERBOSE = Boolean.getBoolean("com.hazelcast.examples.hdjcache.hdnearcache.verbose");

    private void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInvalidateOnChange(true);

        HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1
                = createHiDensityCacheWithHiDensityNearCache(nearCacheConfig);
        HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2
                = createHiDensityCacheWithHiDensityNearCache(nearCacheConfig);

        // put records to cache through client-1
        putRecordsToCacheOnClient1(clientCacheContext1, clientCacheContext2);

        // get records from cache through client-2
        getRecordsFromCacheOnClient2(clientCacheContext1, clientCacheContext2);

        // get records from Near Cache on client-2
        getRecordsFromNearCacheOnClient2(clientCacheContext1, clientCacheContext2);

        // update records at cache through client-1
        updateRecordsInCacheOnClient1(clientCacheContext1, clientCacheContext2);

        // wait a little for invalidation events
        sleepSeconds(5);

        // get invalidated records from Near Cache on client-2
        getInvalidatedRecordsFromNearCacheOnClient2(clientCacheContext1, clientCacheContext2);

        shutdown();
    }

    private void putRecordsToCacheOnClient1(HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1,
                                            HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2) {
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i);
            clientCacheContext1.cache.put(i, value);
            if (VERBOSE) {
                System.out.println("Put key=" + i + ", value=" + value + " to Hi-Density cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been put to Hi-Density cache through client-1");
        System.out.println("Memory usage on client-1: " + clientCacheContext1.memoryManager.getMemoryStats());
        System.out.println("Memory usage on client-2: " + clientCacheContext2.memoryManager.getMemoryStats());
    }

    private void getRecordsFromCacheOnClient2(HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1,
                                              HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // these get() calls populate the Near Cache, so at the next calls,
            // the values will be taken from local Near Cache without any remote access
            String actualValue = clientCacheContext2.cache.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from Hi-Density cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from Hi-Density cache through client-2");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from Hi-Density cache finished in " + elapsed + " milliseconds");
        System.out.println("Memory usage on client-1: " + clientCacheContext1.memoryManager.getMemoryStats());
        System.out.println("Memory usage on client-2: " + clientCacheContext2.memoryManager.getMemoryStats());
    }

    private void getRecordsFromNearCacheOnClient2(HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1,
                                                  HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // since this record has been put to Near Cache before,
            // it is taken from the local Near Cache without any remote access
            String actualValue = clientCacheContext2.cache.get(i);
            String expectedValue = generateValueFromKey(i);
            assert actualValue.equals(expectedValue)
                    : "Taken value from cache must be " + expectedValue + " but it is " + actualValue;
            if (VERBOSE) {
                System.out.println("Get key=" + i + ", value=" + actualValue + " from Hi-Density Near Cache on client-2");
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        System.out.println("Get records from Near Cache finished in " + elapsed + " milliseconds");
        System.out.println("Memory usage on client-1: " + clientCacheContext1.memoryManager.getMemoryStats());
        System.out.println("Memory usage on client-2: " + clientCacheContext2.memoryManager.getMemoryStats());
    }

    private void updateRecordsInCacheOnClient1(HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1,
                                               HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2) {
        for (int i = 0; i < RECORD_COUNT; i++) {
            String value = generateValueFromKey(i * i);
            clientCacheContext1.cache.put(i, value);
            if (VERBOSE) {
                System.out.println("Update key=" + i + ", value=" + value + " to Hi-Density cache through client-1");
            }
        }
        System.out.println(RECORD_COUNT + " records have been updated in Hi-Density cache through client-1");
        System.out.println("Memory usage on client-1: " + clientCacheContext1.memoryManager.getMemoryStats());
        System.out.println("Memory usage on client-2: " + clientCacheContext2.memoryManager.getMemoryStats());
    }

    private void getInvalidatedRecordsFromNearCacheOnClient2(
            HiDensityNearCacheSupportContext<Integer, String> clientCacheContext1,
            HiDensityNearCacheSupportContext<Integer, String> clientCacheContext2) {
        long started = System.nanoTime();
        for (int i = 0; i < RECORD_COUNT; i++) {
            // these record have been invalidated at Near Cache on client-2,
            // because client-1 has updated the records and invalidation events are sent to client-2,
            // so the records have been taken from the remote cache (not the local Near Cache) through client-2
            String actualValue = clientCacheContext2.cache.get(i);
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
        System.out.println("Memory usage on client-1: " + clientCacheContext1.memoryManager.getMemoryStats());
        System.out.println("Memory usage on client-2: " + clientCacheContext2.memoryManager.getMemoryStats());
    }

    public static void main(String[] args) {
        ClientHiDensityNearCacheUsage clientHiDensityNearCacheUsage = new ClientHiDensityNearCacheUsage();
        clientHiDensityNearCacheUsage.run();
    }
}
