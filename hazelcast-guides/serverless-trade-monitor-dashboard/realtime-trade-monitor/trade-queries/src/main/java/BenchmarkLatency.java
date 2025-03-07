import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BenchmarkLatency {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void benchmark(HazelcastInstance hzInstance) throws InterruptedException {
        IMap<String, HazelcastJsonValue> trades = hzInstance.getMap("trades");
        trades.addEntryListener(new TradeRecordsListener(), true);
        Thread.currentThread().join();
    }

    private static class TradeRecordsListener implements EntryAddedListener<String, HazelcastJsonValue> {

        @Override
        public void entryAdded(EntryEvent<String, HazelcastJsonValue> event) {
            try {
                long now = System.currentTimeMillis();
                JsonNode json = MAPPER.readTree(event.getValue().toString());
                System.out.println(now - json.get("timestamp").asLong());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
