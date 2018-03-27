package member;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import com.hazelcast.cache.ICache;

public class JCacheSample {

    public static void main(String[] args) {
        // Run as a Hazelcast Member
        System.setProperty("hazelcast.jcache.provider.type", "server");
        // Create the JCache CacheManager
        CacheManager manager = Caching.getCachingProvider().getCacheManager();
        MutableConfiguration<String, String> configuration = new MutableConfiguration<String, String>();
        // Expire entries after 1 minute
        configuration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        // Get a Cache called "myCache" and configure with 1 minute expiry
        Cache<String, String> myCache = manager.createCache("myCache", configuration);
        // Put and Get a value from "myCache"
        myCache.put("key", "value");
        String value = myCache.get("key");
        System.out.println(value);
        //ICache is a Hazelcast interface that extends JCache, provides more functionality
        ICache<String, String> icache = myCache.unwrap(ICache.class);
        //Async Get and Put using ICache interface
        icache.getAsync("key");
        icache.putAsync("key", "value");
        //ICache allows custom expiry per cache entry
        final ExpiryPolicy customExpiryPolicy = AccessedExpiryPolicy.factoryOf(Duration.TEN_MINUTES).create();
        icache.put("key", "newValue", customExpiryPolicy);
        //Size of the Cache should reflect the ICache and JCache operations
        icache.size();
        //Shutdown the underlying Hazelcast Cluster Member
        manager.getCachingProvider().close();
    }
}
