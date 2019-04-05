package your.company.name;

import com.hazelcast.core.IMap;

/**
 * <p>Simple ways of calculating averages and standard
 * deviation. Both essentially work by retrieving every
 * data record.
 * </p>
 * <p>If the data volume is large this could take a
 * while to pull all the values across the network, once
 * in each method.
 * </p>
 * <p>If the data volume is very very large the
 * "{@code keySet()}" operation could return a larger
 * collection than this process could handle, giving
 * an out of memory error.
 * </p>
 * <p>The simple way does at least have the advantage
 * of simplicity, for maintenance.
 * </p>
 */
public class JuniorDeveloper {

    /**
     * <p>Find the average. Take all the values, sum up
     * the field, and divide by the number of values.
     * </p>
     * <p><u>Will return "{@code NaN}" if there are no values.
     * </u></p>
     *
     * @param iMap The map containing {@link Customer} data
     * @return The average of the "{@code satisfaction}" field
     */
    public static double average(IMap<Integer, Customer> iMap) {

        int count = 0;
        double total = 0;

        for (Integer key : iMap.keySet()) {
            count++;
            total += iMap.get(key).getSatisfaction();
        }

        return (total / count);
    }


    /**
     * <p>Sum up the squares of how each satisfaction rating
     * differs from the average.
     * </p>
     *
     * @param iMap The map containing {@link Customer} data
     * @param average The average of the "{@code satisfaction}" field
     * @return The total of squared differences from average
     */
    public static double totalDifferenceSquared(IMap<Integer, Customer> iMap, double average) {

        double total = 0;

        for (Integer key : iMap.keySet()) {
            int satisfaction = iMap.get(key).getSatisfaction();
            double difference = satisfaction - average;
            total += difference * difference;
        }

        return total;
    }
}
