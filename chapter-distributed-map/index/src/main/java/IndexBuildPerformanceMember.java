import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class IndexBuildPerformanceMember {
    public final static int MAP_SIZE = 100000;

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Person> personMap = hz.getMap("persons");

        System.out.println("Generating testdata");
        System.out.println("Starting benchmark");

        int count = 0;
        long startMs = System.currentTimeMillis();
        for (int l = 0; l < 5; l++) {
            for (int k = 0; k < MAP_SIZE; k++) {
                String name = "" + k;
                personMap.put("" + k, new Person(name));
                count++;
            }
            personMap.clear();
            System.out.println("At " + l);
        }

        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (count * 1000d) / durationMs;
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Performance: " + performance + " inserts per second");
    }
}
