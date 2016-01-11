import com.hazelcast.config.Config;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.memory.MemoryUnit;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple test of a cache.
 */
public final class EnterpriseCacheTestServer {

    private static final long STATS_SECONDS = 10;

    private final HazelcastInstance instance;
    private final MemoryStats memoryStats;
    private final ILogger logger;
    private final MemorySize memorySize;

    static {
        System.setProperty("hazelcast.version.check.enabled", "false");
        System.setProperty("hazelcast.socket.bind.any", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("hazelcast.multicast.group", "224.33.55.79");
    }

    private EnterpriseCacheTestServer(String memory) throws IOException {
        this.memorySize = MemorySize.parse(memory, MemoryUnit.GIGABYTES);

        InputStream configInputStream = EnterpriseCacheTestServer.class.getResourceAsStream("/hazelcast-hd-memory.xml");
        Config cfg = new XmlConfigBuilder(configInputStream).build();

        NativeMemoryConfig memoryConfig = cfg.getNativeMemoryConfig();
        if (!memoryConfig.isEnabled()) {
            memoryConfig.setSize(memorySize).setEnabled(true);
            memoryConfig.setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED);
        }

        instance = Hazelcast.newHazelcastInstance(cfg);
        memoryStats = MemoryStatsUtil.getMemoryStats(instance);
        logger = instance.getLoggingService().getLogger(EnterpriseCacheTestServer.class);
    }

    public static void main(String[] input) throws Exception {
        String memory = "3";
        boolean master = false;
        if (input != null && input.length > 0) {
            memory = input[0];
            if (input.length > 1 && input[1].equals("master")) {
                master = true;
            }
        }

        EnterpriseCacheTestServer test = new EnterpriseCacheTestServer(memory);
        test.start();
    }

    private void start() throws InterruptedException {
        printVariables();
        startPrintStats();
    }

    private void startPrintStats() {
        new Thread() {
            {
                setDaemon(true);
                setName("PrintStats." + instance.getName());
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(STATS_SECONDS * 1000);
                        System.out.println("");
                        System.out.println(memoryStats);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }
        }.start();
    }

    private void printVariables() {
        logger.info("Starting Test with ");
        logger.info("Memory: " + memorySize.toPrettyString());
    }
}
