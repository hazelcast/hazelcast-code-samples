import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.Random;
import java.util.concurrent.TimeUnit;

//If you want to create a really distributed test, start a few AdditionalMember instances.
public class IndexBenchmark {
    public final static int MAP_SIZE = 100000;
    public final static int TIME_SECONDS = 60;
    public final static int UPDATE_PERCENTAGE = 10;
    private static final String[] names = new String[]{"Jacob", "Sophia", "Mason", "Isabella",
            "William", "Emma", "Jayden", "Olivia", "Noah", "Ava", "Michael", "Emily",
            "Ethan", "Abigail", "Alexander", "Madison", "Aiden", "Mia", "Daniel", "Chloe"};

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        test(hz, true);
        test(hz, false);
    }

    public static void test(HazelcastInstance hz, boolean indexEnabled) {
        IMap personMap = hz.getMap(indexEnabled ? "personsWithIndex" : "personsWithoutIndex");

        System.out.println("===============================================");
        System.out.println("Index enabled: " + indexEnabled);

        System.out.println("Generating testdata");
        Random random = new Random();
        for (int k = 0; k < MAP_SIZE; k++) {
            String name = names[random.nextInt(names.length)];
            personMap.put("" + k, new Person(name));
        }
        System.out.println("Testdata generated");

        System.out.println("Starting benchmark");
        long searchCount = 0;
        long updateCount = 0;

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIME_SECONDS);
        while (System.currentTimeMillis() < endTime) {
            int x = random.nextInt(100);
            if (x < UPDATE_PERCENTAGE) {
                int id = random.nextInt(MAP_SIZE);
                String name = names[random.nextInt(names.length)];
                personMap.put("" + id, new Person(name));
                updateCount++;
            } else {
                Predicate predicate = Predicates.equal("name", names[random.nextInt(names.length)]);
                personMap.values(predicate);
                searchCount++;
            }
        }
        System.out.println("Benchmark complete");

        long totalCount = searchCount + updateCount;
        System.out.println("Index enabled: " + indexEnabled);
        System.out.println("Distributed searches: " + (hz.getCluster().getMembers().size() > 1));
        System.out.println("Update percentage: " + UPDATE_PERCENTAGE);

        System.out.println("Total map size: " + personMap.size());
        System.out.println("Total searches: " + searchCount);
        System.out.println("Total updates: " + updateCount);

        System.out.println("Performance : " + ((totalCount * 1d) / TIME_SECONDS) + " operations per second");
        personMap.destroy();
    }
}
