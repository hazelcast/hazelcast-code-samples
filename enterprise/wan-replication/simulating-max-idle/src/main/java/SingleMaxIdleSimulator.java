import com.hazelcast.config.Config;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.internal.util.Clock;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;

import java.util.Map.Entry;

class SingleMaxIdleSimulator extends AbstractMaxIdleSimulator {
    private static final int MAX_IDLE_SECONDS = 10;
    private final MaxIdleSimulatingGet<String, String> entryProcessor = new MaxIdleSimulatingGet<>();

    public static void main(String[] args) {
        AbstractMaxIdleSimulator simulator = new SingleMaxIdleSimulator();
        simulator.simulate();
    }

    // You may want to consider using IdentifiedDataSerializable instead
    // of default java serialization for better performance. If you do so,
    // EntryProcessor, key and value should extend IdentifiedDataSerializable.
    // https://docs.hazelcast.com/hazelcast/5.2/serialization/implementing-dataserializable#identifieddataserializable
    private static class MaxIdleSimulatingGet<K, V> implements EntryProcessor<K, V, V>, HazelcastInstanceAware {
        private transient HazelcastInstance instance;
        private transient boolean readOnly = true;

        @SuppressWarnings("checkstyle:linelength")
        @Override
        public V process(Entry<K, V> entry) {
            EntryView<K, V> entryView = instance.<K, V>getMap(MAP_NAME).getEntryView(entry.getKey());

            if (entryView == null) {
                // We can reach here if there is a network partition or member crash.
                // Also, because backup entry processor is the same as this processor,
                // we can reach here if there is any discrepancies between backup and
                // primary. However, this isn't a problem. See the note here:
                // https://docs.hazelcast.com/hazelcast/5.2/computing/entry-processor#processing-backup-entries
                return null;
            } else if (Clock.currentTimeMillis() - entryView.getLastUpdateTime() > MAX_IDLE_SECONDS) {
                // Before https://github.com/hazelcast/hazelcast/pull/23279 to access
                // last-update-time (entryView.getLastUpdateTime() used here),
                // per-entry-stats of map-config should be enabled.
                // https://docs.hazelcast.com/hazelcast/5.2/data-structures/reading-map-metrics#getting-statistics-about-a-specific-map-entry
                readOnly = false;
                return entry.setValue(entry.getValue());
            } else {
                return entry.getValue();
            }
        }

        @Override
        public EntryProcessor<K, V, V> getBackupProcessor() {
            return readOnly ? null : this;
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
            instance = hazelcastInstance;
        }
    }

    @Override
    void modifyConfig(Config config) {
        config.getMapConfig(MAP_NAME).setTimeToLiveSeconds(maxIdleToTtl(MAX_IDLE_SECONDS));
    }

    @Override
    int getMaxIdleSeconds() {
        return MAX_IDLE_SECONDS;
    }

    @Override
    String simulateGet(IMap<String, String> map, String key, boolean mapInA) {
        return map.executeOnKey(key, entryProcessor);
    }

    @Override
    boolean throwIfNonExpiredDataIsMissing() {
        return true;
    }

    @Override
    boolean bounceMembers() {
        return false;
    }

    @Override
    boolean migrating() {
        return false;
    }
}
