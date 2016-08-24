import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class Container {

    private final ConcurrentMap<String, Integer> counterMap = new ConcurrentHashMap<String, Integer>();

    int inc(String id, int amount) {
        Integer counter = counterMap.get(id);
        if (counter == null) {
            counter = 0;
        }
        counter += amount;
        counterMap.put(id, counter);
        return counter;
    }

    void await(String id, int amount) {
        try {
            Integer counter = counterMap.get(id);
            while (counter == null || counter < amount) {
                MILLISECONDS.sleep(10);
                counter = counterMap.get(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void clear() {
        counterMap.clear();
    }

    void applyMigrationData(Map<String, Integer> migrationData) {
        counterMap.putAll(migrationData);
    }

    Map<String, Integer> toMigrationData() {
        return new HashMap<String, Integer>(counterMap);
    }
}
