import java.util.HashMap;
import java.util.Map;

class Container {

    private final Map<String, Integer> values = new HashMap<String, Integer>();

    int inc(String id, int amount) {
        Integer counter = values.get(id);
        if (counter == null) {
            counter = 0;
        }
        counter += amount;
        values.put(id, counter);
        return counter;
    }

    void clear() {
        values.clear();
    }

    void applyMigrationData(Map<String, Integer> migrationData) {
        values.putAll(migrationData);
    }

    Map<String, Integer> toMigrationData() {
        return new HashMap<String, Integer>(values);
    }

    void init(String objectName) {
        values.put(objectName, 0);
    }

    void destroy(String objectName) {
        values.remove(objectName);
    }
}
