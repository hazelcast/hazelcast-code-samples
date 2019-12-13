import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class BitmapIndexBenchmark {

    private static final int MAP_SIZE = 100000;
    private static final int TOTAL_HABIT_COUNT = 2000;
    private static final int PERSON_HABIT_COUNT = 200;

    private static final int TIME_SECONDS = 15;
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
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        benchmark(instance, true);
        benchmark(instance, false);

        Hazelcast.shutdownAll();
    }

    private static void benchmark(HazelcastInstance instance, boolean useBitmapIndex) {
        // see resources/hazelcast.xml for the map configuration
        IMap<Integer, Person> personMap;
        if (useBitmapIndex) {
            personMap = instance.getMap("personsWithBitmapIndexOnHabits");
        } else {
            personMap = instance.getMap("personsWithHashIndexOnHabits");
        }

        System.out.println("===============================================");
        System.out.println("Bitmap index enabled: " + useBitmapIndex);

        System.out.println("Generating test data...");
        for (int i = 0; i < MAP_SIZE; i++) {
            personMap.put(i, new Person(generateRandomName(), generateRandomHabits()));
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
                personMap.put(id, new Person(generateRandomName(), generateRandomHabits()));
                updateCount++;
            } else {
                // alternatively, SqlPredicate may be used
                Predicate forenamePredicate = Predicates.equal("habits[any]", randomHabit());
                Predicate surnamePredicate = Predicates.equal("habits[any]", randomHabit());
                Predicate predicate = Predicates.and(forenamePredicate, surnamePredicate);

                personMap.values(predicate);
                searchCount++;
            }
        }
        System.out.println("Benchmark complete!");

        long totalCount = searchCount + updateCount;
        System.out.println("Update percentage: " + UPDATE_PERCENTAGE);

        System.out.println("Total map size: " + personMap.size());
        System.out.println("Total searches: " + searchCount);
        System.out.println("Total updates: " + updateCount);

        System.out.println(format("Performance: %.2f operations per second", ((totalCount * 1d) / TIME_SECONDS)));

        System.gc();
        long usedHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Used heap (approximate): " + usedHeap / (1024 * 1024) + "MB");

        long indexCost = (usedHeap - personMap.getLocalMapStats().getOwnedEntryMemoryCost());
        double bytesPerIndexEntry = (double) indexCost / (MAP_SIZE * PERSON_HABIT_COUNT);
        System.out.println(
                format("Index cost (approximate): %dMB, %.1f bytes per index entry",
                indexCost / (1024 * 1024),
                bytesPerIndexEntry));

        personMap.destroy();
    }

    private static Name generateRandomName() {
        String forename = randomForename();
        String surname = randomSurname();
        return new Name(forename, surname);
    }

    private static int[] generateRandomHabits() {
        int[] habits = new int[PERSON_HABIT_COUNT];
        for (int i = 0; i < PERSON_HABIT_COUNT; ++i) {
            habits[i] = randomHabit();
        }
        return habits;
    }

    private static int randomHabit() {
        return RANDOM.nextInt(TOTAL_HABIT_COUNT);
    }

    private static String randomForename() {
        return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
    }

    private static String randomSurname() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

}
