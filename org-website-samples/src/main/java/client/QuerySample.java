package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;

import java.io.IOException;
import java.util.Collection;

public class QuerySample {
    public static class User implements Portable {

        public static final int CLASS_ID = 1;

        public String username;
        public int age;
        public boolean active;

        public User(String username, int age, boolean active) {
            this.username = username;
            this.age = age;
            this.active = active;
        }

        public User() {

        }

        @Override
        public String toString() {
            return "User{"
                    + "username='" + username + '\''
                    + ", age=" + age
                    + ", active=" + active
                    + '}';
        }

        @Override
        public int getFactoryId() {
            return ThePortableFactory.FACTORY_ID;
        }

        @Override
        public int getClassId() {
            return CLASS_ID;
        }

        @Override
        public void writePortable(PortableWriter writer) throws IOException {
            writer.writeUTF("username", username);
            writer.writeInt("age", age);
            writer.writeBoolean("active", active);
        }

        @Override
        public void readPortable(PortableReader reader) throws IOException {
            username = reader.readUTF("username");
            age = reader.readInt("age");
            active = reader.readBoolean("active");
        }
    }

    public static class ThePortableFactory implements PortableFactory {

        public static final int FACTORY_ID = 1;

        @Override
        public Portable create(int classId) {
            if (classId == User.CLASS_ID) {
                return new User();
            }
            return null;
        }
    }

    private static void generateUsers(IMap<String, User> users) {
        users.put("Rod", new User("Rod", 19, true));
        users.put("Jane", new User("Jane", 20, true));
        users.put("Freddy", new User("Freddy", 23, true));
    }

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig()
                .addPortableFactory(ThePortableFactory.FACTORY_ID, new ThePortableFactory());
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
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
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
