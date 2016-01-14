import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.cluster.ClusterState;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.HotRestartPersistenceConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.IOUtil;

import javax.cache.Cache;
import javax.cache.spi.CachingProvider;
import java.io.File;

public class JCacheHotRestartMultipleNodes {

    private static final String LICENSE_KEY = "---- LICENSE KEY ----";

    private static final String HOT_RESTART_ROOT_DIR = System.getProperty("java.io.tmpdir")
            + File.separatorChar + "hazelcast-hot-restart";

    public static void main(String[] args) {
        IOUtil.delete(new File(HOT_RESTART_ROOT_DIR));

        HazelcastInstance instance1 = newHazelcastInstance(5701);
        HazelcastInstance instance2 = newHazelcastInstance(5702);

        Cache<Integer, String> cache = createCache(instance1);
        for (int i = 0; i < 50; i++) {
            cache.put(i, "value" + i);
        }

        instance2.getCluster().shutdown();

        // Offloading to a thread.
        // Because all instances should start in parallel
        // to be able to do hot-restart cluster verification
        new Thread() {
            public void run() {
                newHazelcastInstance(5701);
            }
        }.start();

        instance2 = newHazelcastInstance(5702);
        instance2.getCluster().changeClusterState(ClusterState.ACTIVE);

        cache = createCache(instance2);
        for (int i = 0; i < 50; i++) {
            System.out.println("cache.get(" + i + ") = " + cache.get(i));
        }

        Hazelcast.shutdownAll();
    }

    private static HazelcastInstance newHazelcastInstance(int port) {
        Config config = new Config();
        config.setLicenseKey(LICENSE_KEY);

        config.getNetworkConfig().setPort(port).setPortAutoIncrement(false);
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).clear()
                .addMember("127.0.0.1:5701")
                .addMember("127.0.0.1:5702");

        HotRestartPersistenceConfig hotRestartConfig = config.getHotRestartPersistenceConfig();
        hotRestartConfig.setEnabled(true).setBaseDir(new File(HOT_RESTART_ROOT_DIR));

        return Hazelcast.newHazelcastInstance(config);
    }

    private static Cache<Integer, String> createCache(HazelcastInstance instance) {
        CachingProvider cachingProvider = HazelcastServerCachingProvider
            .createCachingProvider(instance);

        CacheConfig<Integer, String> cacheConfig = new CacheConfig<Integer, String>("cache");
        cacheConfig.getHotRestartConfig().setEnabled(true);

        return cachingProvider.getCacheManager().createCache("cache", cacheConfig);
    }
}
