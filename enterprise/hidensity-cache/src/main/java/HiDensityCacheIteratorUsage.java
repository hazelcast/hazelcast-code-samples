import com.hazelcast.cache.ICache;

import javax.cache.Cache;
import java.util.Iterator;

/**
 * HiDensity cache {@link Iterator} usage example.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class HiDensityCacheIteratorUsage extends HiDensityCacheUsageSupport {

    private static final int SIZE = 1000;

    public static void main(String[] args) {
        init();

        try {
            ICache<Integer, Integer> cache = createCache("MyCacheForIteratorUsage");
            for (int i = 0; i < SIZE; i++) {
                cache.put(i, i * i);
            }

            Iterator<Cache.Entry<Integer, Integer>> iter = cache.iterator();
            while (iter.hasNext()) {
                Cache.Entry<Integer, Integer> e = iter.next();
                int key = e.getKey();
                int value = e.getValue();
                System.out.println("Key: " + key + ", Value: " + value);
            }

            cache.destroy();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            destroy();
        }
    }
}
