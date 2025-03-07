import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import static com.hazelcast.jet.Util.entry;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

public final class LoadSymbols {

    public static void loadSymbols(HazelcastInstance hzInstanse) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Trade.class.getResourceAsStream("/nasdaqlisted.txt"), UTF_8))
        ) {
            IMap<String, String> symbols = hzInstanse.getMap("symbols");
            Map<String, String> map = reader.lines()
                                                .skip(1)
                                                .map(l -> {
                                                    String[] split = l.split("\\|");
                                                    return entry(split[0], split[1]);
                                                }).collect(toMap(Entry::getKey, Entry::getValue));
            long start = System.nanoTime();
            symbols.putAll(map);
            long elapsed = System.nanoTime() - start;
            System.out.println("Loaded " + map.size() + " symbols into map '" + symbols.getName() + "' in " + TimeUnit.NANOSECONDS.toMillis(elapsed) + "ms");
        }
    }
}
