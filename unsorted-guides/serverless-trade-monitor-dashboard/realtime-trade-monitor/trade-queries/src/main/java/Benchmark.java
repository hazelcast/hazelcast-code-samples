import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.query.impl.predicates.EqualPredicate;

import java.util.concurrent.TimeUnit;

public class Benchmark {

    public static void benchmark(HazelcastInstance hzInstance) {
        IMap<String, String> symbols = hzInstance.getMap("symbols");
        IMap<String, HazelcastJsonValue> trades = hzInstance.getMap("trades");

        int count = symbols.size();
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            symbols.keySet().forEach(symbol -> {
                trades.values(new EqualPredicate("symbol", symbol));
            });
            long elapsed = System.nanoTime() - start;
            System.out.println("Did " + count + " queries in " + TimeUnit.NANOSECONDS.toMillis(elapsed) + "ms");
        }
    }
}
