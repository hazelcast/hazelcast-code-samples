import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import com.hazelcast.mapreduce.Mapper;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple example that sums the numbers 1 to 10000.
 */
public class BasicMapReduce {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try {
            HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
            Hazelcast.newHazelcastInstance();
            Hazelcast.newHazelcastInstance();

            // create a default map
            IMap<Integer, Integer> m1 = hz1.getMap("default");
            for (int i = 0; i < 10000; i++) {
                m1.put(i, i);
            }

            // create a job tracker with default config
            JobTracker tracker = hz1.getJobTracker("myJobTracker");

            // using a built-in source from our IMap. This supplies key value pairs
            KeyValueSource<Integer, Integer> kvs = KeyValueSource.fromMap(m1);

            // create a new Job with our source
            Job<Integer, Integer> job = tracker.newJob(kvs);

            // configure the job
            ICompletableFuture<Map<String, Integer>> myMapReduceFuture
                    = job.mapper(new MyMapper()).reducer(new MyReducerFactory()).submit();

            Map<String, Integer> result = myMapReduceFuture.get();

            System.out.println("The sum of the numbers 1 to 10000 is: " + result.get("all_values"));
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    /**
     * My mapper emits a key value pair per map key. An IMap only ever has one.
     * <p/>
     * As I want to do a sum, I am going to accumulate all of these to one key called "all_values".
     * Unfortunately, this maps all to one node. If we were doing a classic group by, we would get
     * parallelization.
     */
    private static class MyMapper implements Mapper<Integer, Integer, String, Integer> {

        @Override
        public void map(Integer key, Integer value, Context<String, Integer> context) {
            context.emit("all_values", value);
        }
    }

    /**
     * Returns a Reducer. Multiple reducers run on one Node, therefore we must provide a factory.
     */
    private static class MyReducerFactory implements ReducerFactory<String, Integer, Integer> {

        @Override
        public Reducer<Integer, Integer> newReducer(String key) {
            return new MyReducer();
        }
    }

    /**
     * Reduces to a sum. One of these if created per key.
     */
    private static class MyReducer extends Reducer<Integer, Integer> {

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
