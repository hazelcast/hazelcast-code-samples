import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Container {
    private final ConcurrentMap<String, Integer> counterMap = new ConcurrentHashMap<>();

    int inc(String id, int amount) {
        Integer counter = counterMap.get(id);
        if (counter == null) {
            counter = 0;
        }
        counter += amount;
        counterMap.put(id, counter);
        return counter;
    }
}
