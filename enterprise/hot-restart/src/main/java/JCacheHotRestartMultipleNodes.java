import com.hazelcast.cluster.ClusterState;
import com.hazelcast.config.Config;
import com.hazelcast.config.HotRestartPersistenceConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.nio.IOUtil;

import javax.cache.Cache;
import java.io.File;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class JCacheHotRestartMultipleNodes {

    private static final String HOT_RESTART_ROOT_DIR = System.getProperty("java.io.tmpdir")
            + File.separatorChar + "hazelcast-hot-restart";

    public static void main(String[] args) {
        IOUtil.delete(new File(HOT_RESTART_ROOT_DIR + "5701"));
        IOUtil.delete(new File(HOT_RESTART_ROOT_DIR + "5702"));

        HazelcastInstance instance1 = newHazelcastInstance(5701);
        HazelcastInstance instance2 = newHazelcastInstance(5702);

        Cache<Integer, String> cache = JCacheHotRestart.createCache(instance1);
        for (int i = 0; i < 50; i++) {
            cache.put(i, "value" + i);
        }

        instance2.getCluster().shutdown();

        // Offloading to a thread.
        // Because all instances should start in parallel
        // to be able to do hot-restart cluster verification
        new Thread(() -> newHazelcastInstance(5701)).start();

        instance2 = newHazelcastInstance(5702);
        instance2.getCluster().changeClusterState(ClusterState.ACTIVE);

        cache = JCacheHotRestart.createCache(instance2);
        for (int i = 0; i < 50; i++) {
            System.out.println("cache.get(" + i + ") = " + cache.get(i));
        }

        Hazelcast.shutdownAll();
    }

    private static HazelcastInstance newHazelcastInstance(int port) {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);

        config.getNetworkConfig().setPort(port).setPortAutoIncrement(false);
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).clear()
                .addMember("127.0.0.1:5701")
                .addMember("127.0.0.1:5702");

        HotRestartPersistenceConfig hotRestartConfig = config.getHotRestartPersistenceConfig();
        hotRestartConfig.setEnabled(true).setBaseDir(new File(HOT_RESTART_ROOT_DIR + port));

        return Hazelcast.newHazelcastInstance(config);
    }
}
