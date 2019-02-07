import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class CompositeIndexBenchmark {

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

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        // if you want to create a really distributed benchmark, start a few
        // AdditionalMember instances before running the benchmark
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        benchmark(instance, true);
        benchmark(instance, false);

        Hazelcast.shutdownAll();
    }

    private static void benchmark(HazelcastInstance instance, boolean useCompositeIndex) {
        // see resources/hazelcast.xml for the map configuration
        IMap<Integer, Person> personMap;
        if (useCompositeIndex){
            personMap = instance.getMap("personsWithCompositeIndex");
        }
        else {
            personMap = instance.getMap("personsWithIndex");
        }

        System.out.println("===============================================");
        System.out.println("Composite index enabled: " + useCompositeIndex);

        System.out.println("Generating test data...");
        for (int i = 0; i < MAP_SIZE; i++) {
            personMap.put(i, new Person(generateRandomName()));
        }
        System.out.println("Test data generated!");

        System.out.println(format("Benchmarking (%d seconds)...", TIME_SECONDS));
        long searchCount = 0;
        long updateCount = 0;

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIME_SECONDS);
        while (System.currentTimeMillis() < endTime) {
            int x = RANDOM.nextInt(100);
            if (x < UPDATE_PERCENTAGE) {
                int id = RANDOM.nextInt(MAP_SIZE);
                personMap.put(id, new Person(generateRandomName()));
                updateCount++;
            } else {
                // alternatively, SqlPredicate may be used
                Predicate forenamePredicate = Predicates.equal("name.forename", randomForename());
                Predicate surnamePredicate = Predicates.equal("name.surname", randomSurname());
                Predicate predicate = Predicates.and(forenamePredicate, surnamePredicate);

                personMap.values(predicate);
                searchCount++;
            }
        }
        System.out.println("Benchmark complete!");

        long totalCount = searchCount + updateCount;
        System.out.println("Composite index enabled: " + useCompositeIndex);
        System.out.println("Distributed searches: " + (instance.getCluster().getMembers().size() > 1));
        System.out.println("Update percentage: " + UPDATE_PERCENTAGE);

        System.out.println("Total map size: " + personMap.size());
        System.out.println("Total searches: " + searchCount);
        System.out.println("Total updates: " + updateCount);

        System.out.println(format("Performance: %.2f operations per second", ((totalCount * 1d) / TIME_SECONDS)));
        personMap.destroy();
    }

    private static Name generateRandomName() {
        String forename = randomForename();
        String surname = randomSurname();
        return new Name(forename, surname);
    }

    private static String randomForename() {
        return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
    }

    private static String randomSurname() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

}
