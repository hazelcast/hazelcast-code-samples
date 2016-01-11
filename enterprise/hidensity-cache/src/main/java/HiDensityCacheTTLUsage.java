import com.hazelcast.cache.ICache;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.util.concurrent.TimeUnit;

/**
 * HiDensity cache TTL usage example
 */
public class HiDensityCacheTTLUsage extends HiDensityCacheUsageSupport {

    private static ExpiryPolicy ttlToExpiryPolicy(long ttl, TimeUnit timeUnit) {
        return new ModifiedExpiryPolicy(new Duration(timeUnit, ttl));
    }

    public static void main(String[] args) {
        init();

        try {
            ICache<String, String> cache = createCache("MyCacheForTTLUsage");

            // ====================================================================== //

            cache.put("key", "value1", ttlToExpiryPolicy(1, TimeUnit.SECONDS));
            System.out.println("Put value \"value1\" with key \"key\" and with TTL 1 seconds");

            System.out.println("Get value with key \"key1\": " + cache.get("key"));

            System.out.println("Size of cache: " + cache.size());

            System.out.println("Wait 1.5 seconds ...");

            // wait 1.5 seconds (0.5 second threshold)
            Thread.sleep(1500);

            System.out.println("Get value with key \"key1\": " + cache.get("key"));

            System.out.println("Size of cache: " + cache.size());

            System.out.println("\n==================================================");

            // ====================================================================== //

            cache.put("key", "value1", new CreatedExpiryPolicy(Duration.ZERO));
            System.out.println("Put value \"value1\" with key \"key\" and with zero expiry duration");

            // should not be there because expire duration is "0"

            System.out.println("Get value with key \"key1\": " + cache.get("key"));

            System.out.println("Size of cache: " + cache.size());

            System.out.println("\n==================================================");

            // ====================================================================== //

            cache.put("key", "value1", new CreatedExpiryPolicy(Duration.ETERNAL));
            System.out.println("Put value \"value1\" with key \"key\" and with eternal expiry duration");

            System.out.println("Get value with key \"key1\": " + cache.get("key"));

            System.out.println("Size of cache: " + cache.size());

            // wait 10 seconds or more, doesn't matter, because expire duration is "ETERNAL"
            // Thread.sleep(10000);

            System.out.println("Get value with key \"key1\": " + cache.get("key"));

            System.out.println("Size of cache: " + cache.size());

            // ====================================================================== //

            cache.destroy();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            destroy();
        }
    }
}
