import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalIndexStats;
import com.hazelcast.query.Predicates;

import java.util.Map;
import java.util.Random;

public class IndexStatisticsDemo {

    private static final int INSERT_COUNT = 1000;
    private static final int UPDATE_COUNT = 50;
    private static final int REMOVE_COUNT = 10;
    private static final int QUERY_COUNT = 100;

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
        // 1. Start a new Hazelcast instance.

        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        // 2. Obtain and populate the personsWithIndex map, see
        // resources/hazelcast.xml for its configuration.

        IMap<Integer, Person> persons = hz.getMap("personsWithIndex");
        for (int personId = 0; personId < INSERT_COUNT; ++personId) {
            Person person = new Person(generateRandomName());
            persons.put(personId, person);
        }

        // 3. Perform some updates.

        for (int personId = 0; personId < UPDATE_COUNT; ++personId) {
            Person person = new Person(generateRandomName());
            persons.put(personId, person);
        }

        // 4. Perform some removals.

        for (int personId = 0; personId < REMOVE_COUNT; ++personId) {
            persons.remove(personId);
        }

        // 5. Run some queries on the map.

        for (int i = 0; i < QUERY_COUNT; ++i) {
            persons.values(Predicates.sql("name.surname = '" + randomSurname() + "'"));
        }

        // 6. Obtain and print the local index statistics.

        Map<String, LocalIndexStats> indexStatsMap = persons.getLocalMapStats().getIndexStats();
        for (Map.Entry<String, LocalIndexStats> indexStatsEntry : indexStatsMap.entrySet()) {
            String indexName = indexStatsEntry.getKey();
            LocalIndexStats indexStats = indexStatsEntry.getValue();

            System.out.println("Index: " + indexName);

            System.out.println("\tQuery Count:\t\t\t\t" + indexStats.getQueryCount());
            System.out.println("\tHit Count:\t\t\t\t\t" + indexStats.getHitCount());

            System.out.println("\tAverage Hit Latency:\t\t" + indexStats.getAverageHitLatency() + " ns");
            System.out.println("\tAverage Hit Selectivity:\t" + indexStats.getAverageHitSelectivity());

            System.out.println("\tInsert Count:\t\t\t\t" + indexStats.getInsertCount());
            System.out.println(
                    "\tAverage Insert Latency:\t\t" + indexStats.getTotalInsertLatency() / indexStats.getInsertCount() + " ns");

            System.out.println("\tUpdate Count:\t\t\t\t" + indexStats.getUpdateCount());
            System.out.println(
                    "\tAverage Update Latency:\t\t" + indexStats.getTotalUpdateLatency() / indexStats.getUpdateCount() + " ns");

            System.out.println("\tRemove Count:\t\t\t\t" + indexStats.getRemoveCount());
            System.out.println(
                    "\tAverage Remove Latency:\t\t" + indexStats.getTotalRemoveLatency() / indexStats.getRemoveCount() + " ns");

            System.out.println("\tMemory Cost:\t\t\t\t" + indexStats.getMemoryCost() + " bytes");
        }

        // 7. Shutdown the instance.

        Hazelcast.shutdownAll();
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
