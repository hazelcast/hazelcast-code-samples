import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Container {
    final ConcurrentMap<String, Integer> counterMap = new ConcurrentHashMap<>();

    int inc(String id, int amount) {
        Integer counter = counterMap.get(id);
        if (counter == null) {
            counter = 0;
        }
        counter += amount;
        counterMap.put(id, counter);
        return counter;
    }

    void clear() {
        counterMap.clear();
    }

    void applyMigrationData(Map<String, Integer> migrationData) {
        counterMap.putAll(migrationData);
    }

    Map<String, Integer> toMigrationData() {
        return new HashMap<>(counterMap);
    }

    void await(String objectId, int amount) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
