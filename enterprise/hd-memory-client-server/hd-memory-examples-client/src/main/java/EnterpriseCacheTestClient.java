import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.memory.NativeOutOfMemoryError;
import com.hazelcast.util.EmptyStatement;
import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.AtomicHistogram;
import org.HdrHistogram.Histogram;

import javax.cache.CacheManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class EnterpriseCacheTestClient {

    private static final String NAMESPACE = "test";
    private static final long STATS_SECONDS = 10;

    private final String[] values = new String[500];
    private final AtomicBoolean live = new AtomicBoolean(true);

    private final HazelcastInstance instance;
    private final CacheManager cacheManager;
    private final ExecutorService executorService;
    private final ILogger logger;
    private final Stats[] allStats;

    private final int threadCount;
    private final int getPercentage;
    private final int putPercentage;
    private final int keyRange;
    private final long duration;

    static {
        System.setProperty("hazelcast.version.check.enabled", "false");
        System.setProperty("hazelcast.socket.bind.any", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("hazelcast.multicast.group", "224.35.57.79");
    }

    private EnterpriseCacheTestClient(int threadCount, int getPercentage, int putPercentage, int keyRange,
                                      int valueMin, int valueMax, long duration) throws IOException {
        InputStream configInputStream = EnterpriseCacheTestClient.class.getResourceAsStream("/hazelcast-client-hd-memory.xml");
        ClientConfig clientConfig = new XmlClientConfigBuilder(configInputStream).build();

        this.instance = HazelcastClient.newHazelcastClient(clientConfig);
        this.cacheManager = HazelcastClientCachingProvider.createCachingProvider(instance).getCacheManager();
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.logger = Logger.getLogger(getClass());
        this.allStats = new Stats[threadCount];
        for (int i = 0; i < threadCount; i++) {
            allStats[i] = new Stats();
        }

        this.threadCount = threadCount;
        this.getPercentage = getPercentage;
        this.putPercentage = putPercentage;
        this.keyRange = keyRange;
        this.duration = duration;

        buildRandomValues(valueMin, valueMax);
    }

    private void buildRandomValues(int valueMin, int valueMax) {
        Random random = new Random();
        for (int i = 0; i < values.length; i++) {
            int size = random.nextInt(valueMax - valueMin) + valueMin;
            byte[] bb = new byte[size];
            random.nextBytes(bb);
            values[i] = new String(bb);
        }
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public static void main(String[] input) throws Exception {
        int threadCount = 40;
        int valueMin = 100;
        int valueMax = 101;
        int getPercentage = 40;
        int putPercentage = 40;
        int keyRange = Integer.MAX_VALUE;
        long duration = Long.MAX_VALUE;

        if (input != null && input.length > 0) {
            for (String arg : input) {
                arg = arg.trim();
                if (arg.startsWith("vs")) {
                    valueMin = Integer.parseInt(arg.substring(2));
                } else if (arg.startsWith("vx")) {
                    valueMax = Integer.parseInt(arg.substring(2));
                } else if (arg.startsWith("t")) {
                    threadCount = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("g")) {
                    getPercentage = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("p")) {
                    putPercentage = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("k")) {
                    keyRange = Integer.parseInt(arg.substring(1));
                    if (keyRange <= 0) {
                        keyRange = Integer.MAX_VALUE;
                    }
                } else if (arg.startsWith("d")) {
                    duration = TimeUnit.MINUTES.toMillis(Integer.parseInt(arg.substring(1)));
                    if (duration <= 0) {
                        duration = Long.MAX_VALUE;
                    }
                }
            }
        }

        EnterpriseCacheTestClient test = new EnterpriseCacheTestClient(threadCount, getPercentage, putPercentage, keyRange,
                valueMin, valueMax, duration);
        test.start();
    }

    private void start() throws InterruptedException {
        printVariables();
        startPrintStats();
        run();
    }

    private void printVariables() {
        logger.info("Starting Test with ");
        logger.info("Thread Count: " + threadCount);
        logger.info("Get Percentage: " + getPercentage);
        logger.info("Put Percentage: " + putPercentage);
        logger.info("Remove Percentage: " + (100 - (putPercentage + getPercentage)));
    }

    private void startPrintStats() {
        new StatsThread().start();
    }

    private void run() {
        ICache<Object, Object> cache = cacheManager.getCache(NAMESPACE).unwrap(ICache.class);
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(new CacheTestRunnable(allStats[i], cache));
        }
    }

    private class StatsThread extends Thread {

        StatsThread() {
            setDaemon(true);
            setName("PrintStats." + instance.getName());
        }

        @Override
        public void run() {
            long terminate = System.currentTimeMillis() + duration;
            if (terminate < 0) {
                terminate = Long.MAX_VALUE;
            }
            AbstractHistogram totalHistogram = new Histogram(1, TimeUnit.MINUTES.toNanos(1), 3);
            while (true) {
                try {
                    long start = System.currentTimeMillis();
                    if (start >= terminate) {
                        live.set(false);
                        executorService.shutdown();
                        executorService.awaitTermination(2, TimeUnit.MINUTES);
                        instance.shutdown();
                        break;
                    }
                    Thread.sleep(STATS_SECONDS * 1000);
                    long end = System.currentTimeMillis();
                    long interval = end - start;

                    totalHistogram.reset();
                    long getsNow = 0;
                    long putsNow = 0;
                    long removesNow = 0;

                    for (int i = 0; i < threadCount; i++) {
                        Stats stats = allStats[i];
                        getsNow += stats.gets.getAndSet(0);
                        putsNow += stats.puts.getAndSet(0);
                        removesNow += stats.removes.getAndSet(0);

                        totalHistogram.add(stats.histogram);
                        stats.histogram.reset();
                    }
                    long totalOps = getsNow + putsNow + removesNow;

                    totalHistogram.reestablishTotalCount();
                    totalHistogram.outputPercentileDistribution(System.out, 1, 1000d);

                    System.out.println();
                    System.out.println("total-ops= " + (totalOps * 1000 / interval) + ", gets:" + (getsNow * 1000 / interval)
                            + ", puts:" + (putsNow * 1000 / interval) + ", removes:" + (removesNow * 1000 / interval));
                    System.out.println();
                    System.out.println();
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }
    }

    private class CacheTestRunnable implements Runnable {

        private final Random random = new Random();

        private final Stats stats;
        private final ICache<Object, Object> cache;

        CacheTestRunnable(Stats stats, ICache<Object, Object> cache) {
            this.stats = stats;
            this.cache = cache;
        }

        @Override
        public void run() {
            int stopCounter = 0;
            while (true) {
                if (stopCounter++ == 5000) {
                    stopCounter = 0;
                    if (!live.get()) {
                        return;
                    }
                }
                try {
                    Object key = newKey(random);
                    int operation = random.nextInt(100);
                    long start = System.nanoTime();
                    if (operation < getPercentage) {
                        cache.get(key);
                        stats.gets.incrementAndGet();
                    } else if (operation < getPercentage + putPercentage) {
                        try {
                            cache.put(key, values[random.nextInt(values.length)]);
                        } catch (NativeOutOfMemoryError e) {
                            System.err.println(e.getMessage());
                        }
                        stats.puts.incrementAndGet();
                    } else {
                        cache.remove(key);
                        stats.removes.incrementAndGet();
                    }
                    long end = System.nanoTime();

                    try {
                        stats.histogram.recordValue(end - start);
                    } catch (IndexOutOfBoundsException e) {
                        EmptyStatement.ignore(e);
                    }
                } catch (Throwable e) {
                    if (e.getCause() instanceof InterruptedException) {
                        return;
                    }
                    if (!instance.getLifecycleService().isRunning()) {
                        return;
                    }
                    logger.warning(e.getClass().getName() + ": " + e.getMessage(), e);
                }
            }
        }

        private Object newKey(Random rand) {
            return (keyRange <= 0 || keyRange == Integer.MAX_VALUE) ? rand.nextLong() : rand.nextInt(keyRange);
        }

    }

    private static class Stats {

        final AbstractHistogram histogram = new AtomicHistogram(1, TimeUnit.MINUTES.toNanos(1), 3);
        final AtomicInteger gets = new AtomicInteger();
        final AtomicInteger puts = new AtomicInteger();
        final AtomicInteger removes = new AtomicInteger();
    }
}
