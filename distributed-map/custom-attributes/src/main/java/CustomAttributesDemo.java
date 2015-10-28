import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import com.test.customer.Customer;

import java.util.Set;

public class CustomAttributesDemo {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Customer> map = hz.getMap("people");

        map.put("1", new Customer("peter", true, 36));
        map.put("2", new Customer("john", false, 40));
        map.put("3", new Customer("roger", true, 20));

        // We're using a custom 'ageInMonths' attribute here that's provided by the 'AgeInMonthsExtractor'
        Set<Customer> employees = (Set<Customer>) map.values(new SqlPredicate("active AND ageInMonths < 360"));
        System.out.println("Employees:" + employees);
    }
}
