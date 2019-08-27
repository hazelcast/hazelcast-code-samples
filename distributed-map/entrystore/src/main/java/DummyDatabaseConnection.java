import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DummyDatabaseConnection {

    private Map<String, Record> storage = new ConcurrentHashMap<>();

    public void store(String key, String value, long expiryDate) {
        storage.put(key, new Record(key, value, expiryDate));
    }

    public Record lookup(String key) {
        return storage.get(key);
    }

    public void remove(String key) {
        storage.remove(key);
    }

    public Iterable<String> keys() {
        return storage.keySet();
    }

    public class Record {

        private String id;
        private String value;
        private long expiryDate;

        public Record(String id, String value, long expiryDate) {
            this.id = id;
            this.value = value;
            this.expiryDate = expiryDate;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public long getExpiryDate() {
            return expiryDate;
        }
    }
}
