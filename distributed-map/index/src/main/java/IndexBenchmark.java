import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

// if you want to create a really distributed test, start a few AdditionalMember instances
public class IndexBenchmark {

    private static final int MAP_SIZE = 100000;
    private static final int TIME_SECONDS = 60;
    private static final int UPDATE_PERCENTAGE = 10;

    private static final String[] FIRST_NAMES = new String[]{
            "Jacob", "Sophia", "Mason", "Isabella", "William", "Emma", "Jayden", "Olivia", "Noah", "Ava",
            "Michael", "Emily", "Ethan", "Abigail", "Alexander", "Madison", "Aiden", "Mia", "Daniel", "Chloe",
    };
    private static final String[] LAST_NAMES = new String[]{
            "Chaney", "Webb", "Strickland", "Gregory", "Salinas", "Yang", "Meyer", "Nicholson", "Liu", "Andrade",
            "Reynolds", "Shannon", "Pace", "Finley", "Forbes", "Burnett", "Rich", "Mcknight", "Ibarra", "Parrish",
    };

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        test(hz, true);
        test(hz, false);

        Hazelcast.shutdownAll();
    }

    private static void test(HazelcastInstance hz, boolean indexEnabled) {
        IMap<String, Person> personMap = hz.getMap(indexEnabled ? "personsWithIndex" : "personsWithoutIndex");

        System.out.println("===============================================");
        System.out.println("Index enabled: " + indexEnabled);

        System.out.println("Generating testdata...");
        Random random = new Random();
        for (int i = 0; i < MAP_SIZE; i++) {
            String forename = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String surname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            personMap.put("" + i, new Person(new Name(forename, surname)));
        }
        System.out.println("Testdata generated!");

        System.out.println(format("Starting benchmark (%d seconds)...", TIME_SECONDS));
        long searchCount = 0;
        long updateCount = 0;

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIME_SECONDS);
        while (System.currentTimeMillis() < endTime) {
            int x = random.nextInt(100);
            if (x < UPDATE_PERCENTAGE) {
                int id = random.nextInt(MAP_SIZE);
                String forename = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String surname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                personMap.put("" + id, new Person(new Name(forename, surname)));
                updateCount++;
            } else {
                Predicate predicate = Predicates.equal("name.surname", LAST_NAMES[random.nextInt(LAST_NAMES.length)]);

                personMap.values(predicate);
                searchCount++;
            }
        }
        System.out.println("Benchmark complete!");

        long totalCount = searchCount + updateCount;
        System.out.println("Index enabled: " + indexEnabled);
        System.out.println("Distributed searches: " + (hz.getCluster().getMembers().size() > 1));
        System.out.println("Update percentage: " + UPDATE_PERCENTAGE);

        System.out.println("Total map size: " + personMap.size());
        System.out.println("Total searches: " + searchCount);
        System.out.println("Total updates: " + updateCount);

        System.out.println(format("Performance: %.2f operations per second", ((totalCount * 1d) / TIME_SECONDS)));
        personMap.destroy();
    }
}
