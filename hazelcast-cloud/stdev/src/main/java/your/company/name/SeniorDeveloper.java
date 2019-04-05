package your.company.name;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

/**
 * <p>Efficient ways of calculating averages and standard
 * deviation. Both essentially work by running the calculation
 * on the data and bringing only intermediate results back
 * to this JVM.
 * </p>
 * <p>The {@link #average} method uses a Hazelcast built-in. This
 * runs on all Hazelcast servers in parallel.
 * </p>
 * <p>The {@link #totalDifferenceSquared} uses the callable
 * {@link TotalDifferenceSquaredCallable} to calculate a subtotal
 * on each server. One sub-total (8 bytes) comes back across the
 * network from each server, plus some wrapping in a {@code Future},
 * regardless of how many records are on each server. We sum all
 * these sub-totals ourselves.
 * </p>
 */
public class SeniorDeveloper {

    /**
     * <p>Use a Hazelcast built-in calculator to find the average.
     * </p>
     * <p><u>Will give a "{@code NullPointerException}" if there are no values.
     * </u></p>
     *
     * @param iMap The map containing {@link Customer} data
     * @return The average of the "{@code satisfaction}" field
     */
    public static double average(IMap<Integer, Customer> iMap) {
        return iMap.aggregate(Aggregators.integerAvg("satisfaction"));
    }

    /**
     * <p>Calculate a total by submitting a callable task that
     * runs on each Hazelcast server process and returns a
     * sub-total based on that server's data.
     * </p>
     *
     * @param iMap The map containing {@link Customer} data
     * @param average The average of the "{@code satisfaction}" field
     * @param hazelcastInstance The Hazelcast client
     * @return The total of squared differences from average
     */
    public static double totalDifferenceSquared(IMap<Integer, Customer> iMap, double average,
            HazelcastInstance hazelcastInstance) {

        TotalDifferenceSquaredCallable totalDifferenceSquaredCallable =
                new TotalDifferenceSquaredCallable(iMap.getName(), average);

        IExecutorService executorService =
                hazelcastInstance.getExecutorService("default");

        /* Run the callable on all servers in parallel.
         */
        Map<Member, Future<Double>> results =
                executorService.submitToAllMembers(totalDifferenceSquaredCallable);

        double total = 0;

        for (Entry<Member, Future<Double>> entry: results.entrySet()) {
            try {
                total += entry.getValue().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return total;
    }

}
