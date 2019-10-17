import com.hazelcast.cache.ICache;

/**
 * HiDensity cache put/get/remove usage example.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class HiDensityCachePutGetReplaceRemoveClearUsage extends HiDensityCacheUsageSupport {

    @SuppressWarnings("checkstyle:methodlength")
    public static void main(String[] args) {
        init();

        try {
            ICache<String, String> cache = createCache("MyCacheForPutGetRemoveUsage");

            cache.put("key1", "value1");
            System.out.println("Put value \"value1\" with key \"key1\"");

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            System.out.println("Put value \"value1\" with key \"key1\" and get its old value: "
                    + cache.getAndPut("key1", "value2"));

            System.out.println("Size of cache: " + cache.size());

            System.out.println("Remove from cache with key \"key1\": " + cache.remove("key1"));

            cache.put("key1", "value3");
            System.out.println("Put value \"value3\" with key \"key1\"");

            System.out.println("Remove from cache with key \"key1\" and value \"xx\": "
                    + cache.remove("key1", "xx"));

            System.out.println("Remove from cache with key \"key1\" and value \"value3\": "
                    + cache.remove("key1", "value3"));

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            System.out.println("Put value \"value1\" with key \"key1\" if absent: "
                    + cache.putIfAbsent("key1", "value1"));

            System.out.println("Put value \"value1\" with key \"key1\" if absent: "
                    + cache.putIfAbsent("key1", "value1"));

            System.out.println("Remove with key \"key1\" and get its old value: "
                    + cache.getAndRemove("key1"));

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            cache.put("key1", "value1");
            System.out.println("Put value \"value1\" with key \"key1\"");

            System.out.println("Cache contains key \"key1\": " + cache.containsKey("key1"));

            System.out.println("Replace value (associated with key \"key2\") with new value \"value2\": "
                    + cache.replace("key2", "value2"));

            System.out.println("Replace value (associated with key \"key1\") with new value \"value2\": "
                    + cache.replace("key1", "value2"));

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            System.out.println("Replace value (associated with key \"key1\") with new value \"value3\" "
                    + "if its value is \"xx\": " + cache.replace("key1", "xx", "value3"));

            System.out.println("Replace value (associated with key \"key1\") with new value \"value3\" "
                    + "if its value is \"value2\": " + cache.replace("key1", "value2", "value3"));

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            System.out.println("Replace value (associated with key \"key1\") with new value \"value4\" "
                    + "and get its old value: " + cache.getAndReplace("key1", "value4"));

            System.out.println("Get value with key \"key1\": " + cache.get("key1"));

            System.out.println("Size of cache: " + cache.size());

            cache.clear();
            System.out.println("Clear cache");

            System.out.println("Size of cache: " + cache.size());

            cache.put("key1", "value1");
            cache.getAsync("key1")
                 .thenAcceptAsync(v -> System.out.println("Get value as async with key \"key1\": " + v))
                 // ensure the consumer in thenAcceptAsync is done before proceeding further
                 .toCompletableFuture().join();

            cache.putAsync("key1", "value2");
            System.out.println("Put value \"value2\" as async with key \"key1\"");

            while (true) {
                Object value = cache.get("key1");
                if (value.equals("value2")) {
                    System.out.println("Put \"value2\" completed");
                    break;
                }
                Thread.sleep(100);
            }

            cache.getAndPutAsync("key1", "value3")
                 .thenAcceptAsync(v -> {
                     System.out.println("Put value \"value3\" as async with key \"key1\" and get its old value: " + v);
                 })
                 .toCompletableFuture().join();

            cache.removeAsync("key2")
                 .thenAcceptAsync(v -> {
                     System.out.println("Remove as async from cache with key \"key2\": " + v);
                 })
                 .toCompletableFuture().join();

            cache.removeAsync("key1")
                 .thenAcceptAsync(v -> {
                     System.out.println("Remove as async from cache with key \"key1\": " + v);
                 })
                 .toCompletableFuture().join();

            cache.put("key1", "value4");
            System.out.println("Put value \"value4\" with key \"key1\"");

            cache.getAndRemoveAsync("key2")
                 .thenAcceptAsync(v -> {
                     System.out.println("Remove as async with key \"key1\" and get its old value: " + v);
                 })
                 .toCompletableFuture().join();

            cache.getAndRemoveAsync("key1")
                 .thenAcceptAsync(v -> {
                     System.out.println("Remove as async with key \"key1\" and get its old value: " + v);
                 })
                 .toCompletableFuture().join();

            cache.destroy();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            destroy();
        }
    }
}
