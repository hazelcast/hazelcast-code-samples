import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.Random;

public class QueryPerformanceMember {
    public final static int MAP_SIZE = 100000;
    public final static int SEARCH_COUNT = 10000;

    private static final String[] names = new String[]{"Jacob", "Sophia", "Mason", "Isabella",
            "William", "Emma", "Jayden", "Olivia", "Noah", "Ava", "Michael", "Emily",
            "Ethan", "Abigail", "Alexander", "Madison", "Aiden", "Mia", "Daniel", "Chloe"};

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        boolean indexEnabled = true;

        IMap<String, Person> personMap;
        if (indexEnabled) {
            personMap = hz.getMap("persons");
        } else {
            personMap = hz.getMap("persons_");
        }

        System.out.println("Generating testdata");
        Random random = new Random();
        for (int k = 0; k < MAP_SIZE; k++) {
            String name = names[random.nextInt(names.length)];
            personMap.put("" + k, new Person(name));
        }
        System.out.println("Generating testdata completed");

        System.out.println("Running benchmark");
        long startMs = System.currentTimeMillis();
        for (int k = 0; k < SEARCH_COUNT; k++) {
            Predicate predicate = Predicates.equal("name", names[random.nextInt(names.length)]);
            personMap.values(predicate);
        }
        System.out.println("Running benchmark complete");

        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (SEARCH_COUNT * 1000d) / durationMs;
        System.out.println("Index enabled: " + indexEnabled);
        System.out.println("Distributed searches: " + (hz.getCluster().getMembers().size() > 1));
        System.out.println("Total map size: " + personMap.size());
        System.out.println("Total searches: " + SEARCH_COUNT);
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Performance: " + performance + " searches per second");
    }
}
