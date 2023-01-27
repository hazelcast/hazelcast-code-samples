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

    private static class MaxIdleSimulatingGet<K, V> implements EntryProcessor<K, V, V>, HazelcastInstanceAware {
        private transient HazelcastInstance instance;
        private transient volatile boolean readOnly = true;

        @SuppressWarnings("checkstyle:linelength")
        @Override
        public V process(Entry<K, V> entry) {
            EntryView<K, V> entryView = instance.<K, V>getMap(MAP_NAME).getEntryView(entry.getKey());

            if (entryView == null) {
                // we can reach here if there is a network partition or member crash
                return null;
                // Before https://github.com/hazelcast/hazelcast/pull/23279 to access
                // last-update-time, per-entry-stats of map-config should be enabled.
                // https://docs.hazelcast.com/hazelcast/5.2/data-structures/reading-map-metrics#getting-statistics-about-a-specific-map-entry
            } else if (Clock.currentTimeMillis() - entryView.getLastUpdateTime() > MAX_IDLE_SECONDS) {
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
