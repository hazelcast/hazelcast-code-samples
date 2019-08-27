import com.hazelcast.map.EntryStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DummyEntryStore implements EntryStore<String, String> {

    private DummyDatabaseConnection connection;

    public DummyEntryStore(DummyDatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public void store(String key, MetadataAwareValue<String> value) {
        connection.store(key, value.getValue(), value.getExpirationTime());
    }

    @Override
    public void storeAll(Map<String, MetadataAwareValue<String>> map) {
        for (Map.Entry<String, MetadataAwareValue<String>> entry: map.entrySet()) {
            connection.store(entry.getKey(), entry.getValue().getValue(), entry.getValue().getExpirationTime());
        }
    }

    @Override
    public void delete(String key) {
        connection.remove(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key: keys) {
            connection.remove(key);
        }
    }

    @Override
    public MetadataAwareValue<String> load(String key) {
        DummyDatabaseConnection.Record rec = connection.lookup(key);
        if (rec == null) {
            return null;
        }
        return new MetadataAwareValue<>(rec.getValue(), rec.getExpiryDate());
    }

    @Override
    public Map<String, MetadataAwareValue<String>> loadAll(Collection<String> keys) {
        Map<String, MetadataAwareValue<String>> entries = new HashMap<>();
        for (String key: keys) {
            DummyDatabaseConnection.Record rec = connection.lookup(key);
            entries.put(key, new MetadataAwareValue<>(rec.getValue(), rec.getExpiryDate()));
        }
        return entries;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return connection.keys();
    }
}
