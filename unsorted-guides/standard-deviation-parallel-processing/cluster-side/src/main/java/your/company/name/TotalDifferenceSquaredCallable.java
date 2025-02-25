package your.company.name;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

/**
 * <p>A Callable to calculate the total squared differences for some
 * data records from the provided average.
 * </p>
 * <p>The logic is largely the same as {@link JuniorDeveloper#totalDifferenceSquared}.
 * We could use {@code stream()} rather than a {@code for-loop} but the
 * result would be the same.
 * </p>
 * <p>The difference though is that this Callable runs multiple times,
 * once on each Hazelcast member, in parallel. To make this work it
 * uses the "{@code localKeySet()}" method on the map rather than
 * "{@code keySet()}" so that each invocation only examines the
 * data on one Hazelcast member.
 * </p>
 */
public class TotalDifferenceSquaredCallable
    implements Callable<Double>, HazelcastInstanceAware, Serializable {
    private static final long serialVersionUID = 1L;

    private transient HazelcastInstance hazelcastInstance;
    private String mapName;
    private double average;

    /**
     * <p>The "{@code mapName}" and "{@code average}" fields
     * are set locally, by the caller.
     * </p>
     *
     * @param arg0 The map name, "{@code Customer}".
     * @param arg1 The average the values deviate from.
     */
    public TotalDifferenceSquaredCallable(String arg0, double arg1) {
        this.mapName = arg0;
        this.average = arg1;
    }

    /**
     * <p>The "{@code hazelcastInstance}" field is set remotely,
     * on each member where the Callable is run.
     * </p>
     *
     * @param arg0 Injected by Hazelcast when the Callable runs.
     */
    @Override
    public void setHazelcastInstance(HazelcastInstance arg0) {
        this.hazelcastInstance = arg0;
    }

    /**
     * <p>Use the "{@ iMap.localKeySet()}" method to get the
     * keys in the map that are only stored on this JVM. In
     * other words, only examine our slice of the data.
     * </p>
     *
     * @return A non-null {@code Double}.
     */
    @Override
    public Double call() throws Exception {
        IMap<Integer, Customer> iMap = this.hazelcastInstance.getMap(this.mapName);

        double total = 0;

        for (Integer key : iMap.localKeySet()) {
            int satisfaction = iMap.get(key).getSatisfaction();
            double difference = satisfaction - this.average;
            total += difference * difference;
        }

        return total;
    }

}
