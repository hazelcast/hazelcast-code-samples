import com.hazelcast.core.IMap;
import com.hazelcast.monitor.NearCacheStats;

abstract class NearCacheSupport {

    static void printNearCacheStats(IMap<Long, Article> map) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();

        System.out.printf("The Near Cache contains %d entries.%n", stats.getOwnedEntryCount());
        System.out.printf("The first article instance was retrieved from the remote instance (Near Cache misses: %d).%n",
                stats.getMisses());
        System.out.printf(
                "The second and third article instance were retrieved from the local Near Cache (Near Cache hits: %d).%n",
                stats.getHits());
    }

    static void printNearCacheStats(IMap<Integer, Article> map, String message) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
        System.out.printf("%s (%d entries, %d hits, %d misses)%n",
                message, stats.getOwnedEntryCount(), stats.getHits(), stats.getMisses());
    }

    static void waitForNearCacheEntryCount(IMap<Integer, Article> map, int targetSize) {
        long ownedEntries;
        do {
            NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
            ownedEntries = stats.getOwnedEntryCount();
        } while (ownedEntries > targetSize);
    }
}
