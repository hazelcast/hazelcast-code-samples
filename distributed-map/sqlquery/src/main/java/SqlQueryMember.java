import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;

import java.util.Set;

public class SqlQueryMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Customer> map = hz.getMap("map");

        map.put("1", new Customer("peter", true, 36));
        map.put("2", new Customer("john", false, 40));
        map.put("3", new Customer("roger", true, 20));

        Set<Customer> customers = (Set<Customer>) map.values(Predicates.sql("active AND age < 30"));
        System.out.println("Customers: " + customers);

        Hazelcast.shutdownAll();
    }
}
