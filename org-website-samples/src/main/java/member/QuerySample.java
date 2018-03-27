package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;

import java.io.Serializable;
import java.util.Collection;

public class QuerySample {

    public static class User implements Serializable {
        String username;
        int age;
        boolean active;

        public User(String username, int age, boolean active) {
            this.username = username;
            this.age = age;
            this.active = active;
        }

        @Override
        public String toString() {
            return "User{"
                    + "username='" + username + '\''
                    + ", age=" + age
                    + ", active=" + active
                    + '}';
        }
    }

    private static void generateUsers(IMap<String, User> users) {
        users.put("Rod", new User("Rod", 19, true));
        users.put("Jane", new User("Jane", 20, true));
        users.put("Freddy", new User("Freddy", 23, true));
    }

    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get a Distributed Map called "users"
        IMap<String, User> users = hz.getMap("users");
        // Add some users to the Distributed Map
        generateUsers(users);
        // Create a Predicate from a String (a SQL like Where clause)
        Predicate sqlQuery = new SqlPredicate("active AND age BETWEEN 18 AND 21)");
        // Creating the same Predicate as above but with a builder
        Predicate criteriaQuery = Predicates.and(
                Predicates.equal("active", true),
                Predicates.between("age", 18, 21)
        );
        // Get result collections using the two different Predicates
        Collection<User> result1 = users.values(sqlQuery);
        Collection<User> result2 = users.values(criteriaQuery);
        // Print out the results
        System.out.println(result1);
        System.out.println(result2);
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }

}
