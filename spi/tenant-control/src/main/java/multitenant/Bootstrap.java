package multitenant;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import static java.util.Collections.singletonList;

/**
 * Bootstrap a Hazelcast member with a {@link MultiTenantClassLoader} that
 * delegates loading of classes in packages {@code apps.app1} and {@code apps.app2}
 * to their respective {@link TenantClassloader}s.
 */
public class Bootstrap {
    public static void main(String[] args) {
        ClassLoader parent = Bootstrap.class.getClassLoader();
        MultiTenantControlFactory.TENANT_CACHE.set("apps.app1");
        MultiTenantClassLoader multitenantClassLoader = new MultiTenantClassLoader(parent);
        // app1 classloader excludes app2, defines apps.app1.* classes on its own
        multitenantClassLoader.addClassLoader("apps.app1", new TenantClassloader(
                singletonList("apps.app2"), "apps.app1", parent));
        // app2 classloader excludes app1, defines apps.app2.* classes on its own
        multitenantClassLoader.addClassLoader("apps.app2", new TenantClassloader(
                singletonList("apps.app1"), "apps.app2", parent));

        Config config = new Config();
        config.addCacheConfig(new CacheSimpleConfig().setName("*"));
        config.setClassLoader(multitenantClassLoader);

        // start hazelcast member
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
    }
}
