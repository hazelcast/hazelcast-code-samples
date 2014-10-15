import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple example that sums the numbers 1 to 100.
 * Created by gluck on 27/05/2014.
 */
public class BasicMapReduce {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance hz3 = Hazelcast.newHazelcastInstance();

        //Create a default map.
        IMap<Integer, Integer> m1 = hz1.getMap("default");
        for (int i = 0; i < 10000; i++) {
            m1.put(i, i);
        }

        //Create a job tracker with default config.
        JobTracker tracker = hz1.getJobTracker("myJobTracker");

        //Using a built-in source from our IMap. This supplies key value pairs.
        KeyValueSource<Integer, Integer> kvs = KeyValueSource.fromMap(m1);

        //Create a new Job with our source.
        Job<Integer, Integer> job = tracker.newJob(kvs);

        //Configure the job.
        ICompletableFuture<Map<String, Integer>> myMapReduceFuture =
                job.mapper(new MyMapper()).reducer(new MyReducerFactory())
                        .submit();

        Map<String, Integer> result = myMapReduceFuture.get();

        System.out.println("The sum of the numbers 1 to 10,000 is: " + result.get("all_values"));
    }

    /**
     * My mapper emits a key value pair per map key. An IMap only ever has one.
     * <p/>
     * As I want to do a sum, I am going to accumulate all of these to one key called "all_values".
     * Unfortunately, this maps all to one node. If we were doing a classic group by, we would get
     * paralellisation.
     */
    public static class MyMapper implements Mapper<Integer, Integer, String, Integer> {

        @Override
        public void map(Integer key, Integer value, Context<String, Integer> context) {
            context.emit("all_values", value);
        }
    }

    /**
     * Returns a Reducer. Multiple reducers run on one Node, therefore we must provide a factory.
     */
    public static class MyReducerFactory implements ReducerFactory<String, Integer, Integer> {


        @Override
        public Reducer<Integer, Integer> newReducer(String key) {
            return new MyReducer();
        }
    }

    /**
     * Reduces to a sum. One of these if created per key.
     */
    public static class MyReducer extends Reducer<Integer, Integer> {

        private AtomicInteger sum = new AtomicInteger(0);

        @Override
        public void reduce(Integer value) {
            sum.addAndGet(value);
        }

        @Override
        public Integer finalizeReduce() {
            return sum.get();
        }
    }


}
