import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.WanBatchPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanReplicationRef;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.map.IMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractMaxIdleSimulator {
    /**
     * This number indicates buffer period needed for an entry to replicate
     * via WAN Replication. An entry will almost replicate in this amount
     * of time, if there isn't any unusual scenario like network partition.
     */
    private static final int REPLICATES_IN_SECONDS = 5;

    /**
     * Simulation length. I found setting this to a high number (for example
     * 1 hour) and interrupting more useful.
     */
    private static final int SIMULATE_FOR_SECONDS = 3600;

    /**
     * If the simulation is bouncing, these numbers indicate how frequent
     * this bouncing is.
     *
     * @see BouncingSingleMaxIdleSimulator
     */
    private static final int BOUNCE_INTERVAL_MIN_SECONDS = 5;
    private static final int BOUNCE_INTERVAL_MAX_SECONDS = 10;

    /**
     * If a simulated get takes more than this amount of time, it will be
     * logged.
     *
     * @see AbstractMaxIdleSimulator#simulateGetWithTracking(IMap, String, boolean)
     */
    private static final int GET_LOG_THRESHOLD_MILLIS = 10;
    static final String MAP_NAME = "replicated-map-name";
    static final Random RANDOM = new Random(42);

    /**
     * You need to enter a license key to run simulations.
     */
    private static final String LICENCE_KEY = "";

    static ILogger loggerA;
    static ILogger loggerB;

    final void simulate() {
        // configuration for cluster A
        Config configA = new Config()
                .setClusterName("A")
                .setLicenseKey(LICENCE_KEY)
                .setNetworkConfig(new NetworkConfig().setPort(5801))
                .setProperty("hazelcast.initial.min.cluster.size", "3")
                .addWanReplicationConfig(
                        new WanReplicationConfig()
                                .setName("wrConfigA")
                                .addBatchReplicationPublisherConfig(
                                        new WanBatchPublisherConfig()
                                                .setClusterName("B")
                                                .setTargetEndpoints("127.0.0.1:5901, 127.0.0.1:5902, 127.0.0.1:5903")))
                .addMapConfig(
                        new MapConfig(MAP_NAME)
                                .setWanReplicationRef(new WanReplicationRef().setName("wrConfigA")));

        // configuration for cluster B
        Config configB = new Config()
                .setClusterName("B")
                .setLicenseKey(LICENCE_KEY)
                .setNetworkConfig(new NetworkConfig().setPort(5901))
                .setProperty("hazelcast.initial.min.cluster.size", "3")
                .addWanReplicationConfig(
                        new WanReplicationConfig()
                                .setName("wrConfigB")
                                .addBatchReplicationPublisherConfig(
                                        new WanBatchPublisherConfig()
                                                .setClusterName("A")
                                                .setTargetEndpoints("127.0.0.1:5801, 127.0.0.1:5802, 127.0.0.1:5803")))
                .addMapConfig(
                        new MapConfig(MAP_NAME)
                                .setWanReplicationRef(new WanReplicationRef().setName("wrConfigB")));

        modifyConfig(configA);
        modifyConfig(configB);

        // instances are run in parallel for faster startup
        CompletableFuture<HazelcastInstance> futureA1
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configA));
        CompletableFuture<HazelcastInstance> futureA2
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configA));
        CompletableFuture<HazelcastInstance> futureA3
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configA));
        CompletableFuture<HazelcastInstance> futureB1
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configB));
        CompletableFuture<HazelcastInstance> futureB2
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configB));
        CompletableFuture<HazelcastInstance> futureB3
                = CompletableFuture.supplyAsync(() -> Hazelcast.newHazelcastInstance(configB));

        CompletableFuture.allOf(futureA1, futureA2, futureA3, futureB1, futureB2, futureB3).join();

        HazelcastInstance hzA1 = futureA1.join();
        HazelcastInstance hzA2 = futureA2.join();
        HazelcastInstance hzA3 = futureA3.join();
        HazelcastInstance hzB1 = futureB1.join();
        HazelcastInstance hzB2 = futureB2.join();
        HazelcastInstance hzB3 = futureB3.join();

        // bounce the members, see BouncingSingleMaxIdleSimulator
        if (bounceMembers()) {
            CompletableFuture.runAsync(() -> bounceMembers(hzA2, hzA3, configA));
            CompletableFuture.runAsync(() -> bounceMembers(hzB2, hzB3, configB));
        }

        // client configuration for cluster A
        HazelcastInstance clientA = HazelcastClient.newHazelcastClient(
                new ClientConfig()
                        .setClusterName("A")
                        .setProperty("hazelcast.client.operation.backup.timeout.millis", "1000")
                        .setNetworkConfig(
                                new ClientNetworkConfig()
                                        .addAddress("127.0.0.1:5801")
                                        .setRedoOperation(true)));

        // client configuration for cluster B
        HazelcastInstance clientB = HazelcastClient.newHazelcastClient(
                new ClientConfig()
                        .setClusterName("B")
                        .setProperty("hazelcast.client.operation.backup.timeout.millis", "1000")
                        .setNetworkConfig(
                                new ClientNetworkConfig()
                                        .addAddress("127.0.0.1:5901")
                                        .setRedoOperation(true)));

        // map proxies to do the simulation
        IMap<String, String> mapA = clientA.getMap(MAP_NAME);
        IMap<String, String> mapB = clientB.getMap(MAP_NAME);

        // loggers from clients are used
        loggerA = clientA.getLoggingService().getLogger(getClass());
        loggerB = clientB.getLoggingService().getLogger(getClass());

        // this map holds the data to see if an entry is expired or not
        Map<String, ExpiryData> keyToExpiryData = new HashMap<>();

        // everything is done in single thread
        long start = System.currentTimeMillis();
        for (int i = 0; ; i++) {
            long now = System.currentTimeMillis();

            if (now - start > SIMULATE_FOR_SECONDS * 1000) {
                break;
            }

            access(now, keyToExpiryData, mapA, mapB);

            // if migration is ongoing, skip below as it will only block the thread
            if (migrating()) {
                continue;
            }

            // keyA can be accessed from cluster A
            // keyB can be accessed from cluster B
            // keyC can be accessed from both clusters
            if (i % 10 == 0) {
                String keyA = "a" + i;
                String keyB = "b" + i;
                String keyC = "c" + i;

                keyToExpiryData.put(keyA, new ExpiryData(now, getMaxIdleSeconds()));
                keyToExpiryData.put(keyB, new ExpiryData(now, getMaxIdleSeconds()));
                keyToExpiryData.put(keyC, new ExpiryData(now, getMaxIdleSeconds()));

                mapA.set(keyA, keyA, keyToExpiryData.get(keyA).getTtlSeconds(), TimeUnit.SECONDS);
                mapB.set(keyB, keyB, keyToExpiryData.get(keyB).getTtlSeconds(), TimeUnit.SECONDS);
                mapA.set(keyC, keyC, keyToExpiryData.get(keyC).getTtlSeconds(), TimeUnit.SECONDS);
                mapB.set(keyC, keyC, keyToExpiryData.get(keyC).getTtlSeconds(), TimeUnit.SECONDS);

                // we put keyA and keyB to the other clusters to not wait the wan replication to replicate
                mapB.set(keyA, keyA, keyToExpiryData.get(keyA).getTtlSeconds(), TimeUnit.SECONDS);
                mapA.set(keyB, keyB, keyToExpiryData.get(keyB).getTtlSeconds(), TimeUnit.SECONDS);
            }

            // every thousandth iteration we check to see if everything is ok
            if (i % 1000 == 0) {
                int usedSizeA = query(keyToExpiryData, now, mapA);
                int usedSizeB = query(keyToExpiryData, now, mapB);

                int sizeA = mapA.size();
                int sizeB = mapB.size();
                loggerA.info(">> "
                        + "Iteration: " + (i / 1000) + " || "
                        + "Usage A: " + usedSizeA + "/" + sizeA + " " + usedSizeA * 100 / sizeA + "% || "
                        + "Usage B: " + usedSizeB + "/" + sizeB + " " + usedSizeB * 100 / sizeB + "%");
            }
        }

        hzA1.getCluster().shutdown();
        hzB1.getCluster().shutdown();
    }

    /**
     * Bounces members continuously. There is a random amount of time
     * between closing a member and rerunning it again.
     *
     * @param hz1    member to bounce
     * @param hz2    another member to bounce
     * @param config used when rerunning the members
     */
    @SuppressWarnings("BusyWait")
    private void bounceMembers(HazelcastInstance hz1, HazelcastInstance hz2, Config config) {
        long start = System.currentTimeMillis();
        for (int i = 0; ; i++) {
            long now = System.currentTimeMillis();

            if (now - start > (SIMULATE_FOR_SECONDS - BOUNCE_INTERVAL_MAX_SECONDS - 5) * 1000) {
                break;
            }

            try {
                Thread.sleep(BOUNCE_INTERVAL_MIN_SECONDS
                        + RANDOM.nextInt(BOUNCE_INTERVAL_MAX_SECONDS - BOUNCE_INTERVAL_MIN_SECONDS + 1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (i % 4 == 0) {
                hz1.shutdown();
            } else if (i % 4 == 1) {
                hz1 = Hazelcast.newHazelcastInstance(config);
            } else if (i % 4 == 2) {
                hz2.shutdown();
            } else {
                hz2 = Hazelcast.newHazelcastInstance(config);
            }
        }
    }

    /**
     * Goes over all the added keys and ensures if they are not expired in
     * keyToExpiryData, they should be found in the hazelcast cluster.
     *
     * @param keyToExpiryData the map to check if entries are expired
     * @param now             current time in millis
     * @param map             hazelcast map to confirm entries exist, if not expired
     * @return number of non-expired entries
     * @throws RuntimeException if a non-expired entry doesn't exist in hazelcast cluster
     * @see AbstractMaxIdleSimulator#throwIfNonExpiredDataIsMissing()
     */
    private int query(Map<String, ExpiryData> keyToExpiryData,
                      long now,
                      IMap<String, String> map) {

        Iterator<Entry<String, ExpiryData>> iterator = keyToExpiryData.entrySet().iterator();
        int usedSize = 0;

        while (iterator.hasNext()) {
            Entry<String, ExpiryData> entry = iterator.next();
            String key = entry.getKey();
            ExpiryData expiryData = entry.getValue();

            if (expiryData.isNotExpired(now)) {
                if (map.get(key) == null) {
                    if (throwIfNonExpiredDataIsMissing()) {
                        throw new RuntimeException();
                    } else {
                        iterator.remove();
                    }
                }
                usedSize++;
            } else {
                iterator.remove();
            }
        }

        return usedSize;
    }

    /**
     * Randomly accesses/reads a non-expired key.
     *
     * @param now             current time in millis
     * @param keyToExpiryData the map to check if entries are expired
     * @param mapA            map in hazelcast cluster A
     * @param mapB            map in hazelcast cluster B
     * @throws RuntimeException if a non-expired entry doesn't exist in hazelcast cluster
     * @see AbstractMaxIdleSimulator#throwIfNonExpiredDataIsMissing()
     */
    private void access(long now,
                        Map<String, ExpiryData> keyToExpiryData,
                        IMap<String, String> mapA,
                        IMap<String, String> mapB) {
        if (keyToExpiryData.keySet().size() < 1) {
            return;
        }

        // This isn't efficient, but it's not important
        String key = keyToExpiryData
                .keySet()
                .stream()
                .skip(RANDOM.nextInt(keyToExpiryData.keySet().size()))
                .findFirst()
                .orElse(null);

        assert key != null;

        // if key starts with c, it can be accessed from both clusters
        IMap<String, String> map;
        boolean mapInA;
        if (key.startsWith("a")) {
            mapInA = true;
            map = mapA;
        } else if (key.startsWith("b")) {
            mapInA = false;
            map = mapB;
        } else {
            mapInA = RANDOM.nextBoolean();
            map = mapInA ? mapA : mapB;
        }

        ExpiryData expiryData = keyToExpiryData.get(key);
        if (expiryData.isNotExpired(now)) {
            if (simulateGetWithTracking(map, key, mapInA) == null) {
                if (throwIfNonExpiredDataIsMissing()) {
                    throw new RuntimeException();
                } else {
                    keyToExpiryData.remove(key);
                }
            } else {
                expiryData.onAccess(now);
            }
        } else {
            keyToExpiryData.remove(key);
        }
    }

    /**
     * Basically a wrapper around simulateGet() to see if took too long to execute.
     *
     * @param map    the hazelcast map to get the value
     * @param key    the key to call get
     * @param mapInA true, if the key should be accessed from cluster A
     * @return the value from the hazelcast cluster
     * @see AbstractMaxIdleSimulator#simulateGet(IMap, String, boolean)
     */
    private String simulateGetWithTracking(IMap<String, String> map, String key, boolean mapInA) {
        long start = System.nanoTime();

        String value = simulateGet(map, key, mapInA);

        long duration = System.nanoTime() - start;
        long millisInNanos = 1_000_000L;

        if (duration > millisInNanos * GET_LOG_THRESHOLD_MILLIS) {
            if (mapInA) {
                loggerA.info(">> Get took " + duration / millisInNanos + "ms for key " + key);
            } else {
                loggerB.info(">> Get took " + duration / millisInNanos + "ms for key " + key);
            }
        }

        return value;
    }

    /**
     * Modifies the configuration according to inheritors needs.
     *
     * @param config the config object to modify
     */
    abstract void modifyConfig(Config config);

    /**
     * Gets the simulated max idle seconds.
     *
     * @return the max idle seconds.
     * @see AbstractMaxIdleSimulator.ExpiryData
     */
    abstract int getMaxIdleSeconds();

    /**
     * This method simulates the get() call with entry processor. The idea
     * is, with a custom logic, occasionally writes can be done. This way
     * the entry can be replicated.
     *
     * @param map    the hazelcast map to get the value
     * @param key    the key to call get
     * @param mapInA true, if the key should be accessed from cluster A
     * @return the value from the hazelcast cluster
     */
    abstract String simulateGet(IMap<String, String> map, String key, boolean mapInA);

    /**
     * If this is false, all exceptions thrown when a non-expired entry is
     * missing will be suppressed. This is useful for bouncing simulations.
     *
     * @return false, if exceptions need to be suppressed
     */
    abstract boolean throwIfNonExpiredDataIsMissing();

    /**
     * Whether the simulation should bounce the members or not.
     *
     * @return true, if bouncing is enabled
     */
    abstract boolean bounceMembers();

    /**
     * Indicates if there is an ongoing migration.
     *
     * @return true if there is a migration ongoing
     */
    abstract boolean migrating();

    /**
     * Max idle is being simulated via usage of ttl. This method, converts
     * ttl to max idle.
     *
     * @param maxIdle the max idle value to convert in seconds
     * @return the ttl value converted in seconds
     */
    static int maxIdleToTtl(int maxIdle) {
        return maxIdle * 2 + REPLICATES_IN_SECONDS;
    }

    /**
     * Max idle is being simulated via usage of ttl. This method, converts
     * max idle to ttl.
     *
     * @param ttl the ttl value to convert in seconds
     * @return the max idle value converted in seconds
     */
    static int ttlToMaxIdle(int ttl) {
        return (ttl - REPLICATES_IN_SECONDS) / 2;
    }

    /**
     * The metadata hold for each key to see if ir's expired or not.
     * Basically, an enhanced tuple.
     */
    static class ExpiryData {
        private final int maxIdleSeconds;
        private long accessTime;

        ExpiryData(long now, int maxIdleSeconds) {
            this.maxIdleSeconds = maxIdleSeconds;
            this.accessTime = now;
        }

        int getTtlSeconds() {
            return maxIdleToTtl(maxIdleSeconds);
        }

        void onAccess(long now) {
            accessTime = now;
        }

        boolean isNotExpired(long now) {
            return now - accessTime < maxIdleSeconds * 1000L;
        }
    }
}
