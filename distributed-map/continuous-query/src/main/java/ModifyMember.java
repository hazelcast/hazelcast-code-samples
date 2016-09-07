import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ModifyMember {

    public static void main(String[] args) {
        Config config = new Config();
        config.setProperty("hazelcast.map.entry.filtering.natural.event.types", "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IMap<String, Person> map = hz.getMap("map");

        // the following entry will generate an event of type ADDED on the ContinuousQueryMember
        map.put("1", new Person("peter"));
        // the following entry will not generate an event on the ContinuousQueryMember
        map.put("2", new Person("talip"));
        // the following update will generate an event of type ADDED on the ContinuousQueryMember
        // as the new value is within the predicate-matching space, while old value did not match the predicate
        map.put("2", new Person("peter"));
        // the following update will generate an event of type REMOVED on the ContinuousQueryMember
        // as the entry was previously matching the predicate but its updated value no longer matches
        map.put("1", new Person("scott"));
        System.out.println("done");
        System.exit(0);
    }
}
