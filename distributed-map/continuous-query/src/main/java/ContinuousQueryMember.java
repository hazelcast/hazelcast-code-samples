import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import com.hazelcast.query.SqlPredicate;

/**
 * Registers an entry listener with a predicate to continuously query map data as it is updated.
 * Since Hazelcast version 3.7, initializing your Hazelcast instance to publish natural filtering event types (setting property
 * {@code hazelcast.map.entry.filtering.natural.event.types} to {@code true}) will change how
 * entry listeners with predicates are notified about map entry value updates to accommodate the continuous
 * querying use case. The following table compares how a listener is notified about an update to a map entry value
 * under the default backwards-compatible Hazelcast behavior (when the property above is not set
 * or is set to {@code false}) versus when set to {@code true}:
 * <table>
 *     <tr>
 *         <th>&nbsp;</th>
 *         <th>Default</th>
 *         <th>hazelcast.map.entry.filtering.natural.event.types = true</th>
 *     </tr>
 *     <tr>
 *         <td>When old value matches predicate,<br/>new value does not match predicate</td>
 *         <td>No event is delivered to entry listener</td>
 *         <td>{@code REMOVED} event is delivered to entry listener</td>
 *     </tr>
 *     <tr>
 *         <td>When old value matches predicate,<br/>new value matches predicate</td>
 *         <td>{@code UPDATED} event is delivered to entry listener</td>
 *         <td>{@code UPDATED} event is delivered to entry listener</td>
 *     </tr>
 *     <tr>
 *         <td>When old value does not match predicate,<br/>new value does not match predicate</td>
 *         <td>No event is delivered to entry listener</td>
 *         <td>No event is delivered to entry listener</td>
 *     </tr>
 *     <tr>
 *         <td>When old value does not match predicate,<br/>new value matches predicate</td>
 *         <td>{@code UPDATED} event is delivered to entry listener</td>
 *         <td>{@code ADDED} event is delivered to entry listener</td>
 *     </tr>
 * </table>
 *
 * Conceptually, enabling the natural filtering event types property produces events reflecting how map entries are
 * added, updated or removed with regards to the space of values matching the predicate.
 *
 * @see com.hazelcast.map.impl.event.QueryCacheNaturalFilteringStrategy
 * @see com.hazelcast.map.impl.event.DefaultEntryEventFilteringStrategy
 * @see com.hazelcast.map.impl.event.MapEventPublisherImpl
 */
public class ContinuousQueryMember {

    public static void main(String[] args) {
        Config config = new Config();
        config.setProperty("hazelcast.map.entry.filtering.natural.event.types", "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IMap<String, String> map = hz.getMap("map");
        map.addEntryListener(new MyEntryListener(),
                new SqlPredicate("name=peter"), true);
        System.out.println("EntryListener registered");
    }

    static class MyEntryListener
            implements EntryAddedListener<String, String>, EntryUpdatedListener<String, String>,
            EntryRemovedListener<String, String>, EntryEvictedListener<String, String>,
            MapEvictedListener, MapClearedListener {
        @Override
        public void entryAdded(EntryEvent<String, String> event) {
            System.out.println("entryAdded:" + event);
        }

        @Override
        public void entryRemoved(EntryEvent<String, String> event) {
            System.out.println("entryRemoved:" + event);
        }

        @Override
        public void entryUpdated(EntryEvent<String, String> event) {
            System.out.println("entryUpdated:" + event);
        }

        @Override
        public void entryEvicted(EntryEvent<String, String> event) {
            System.out.println("entryEvicted:" + event);
        }

        @Override
        public void mapEvicted(MapEvent event) {
            System.out.println("mapEvicted:" + event);
        }

        @Override
        public void mapCleared(MapEvent event) {
            System.out.println("mapCleared:" + event);

        }
    }
}
