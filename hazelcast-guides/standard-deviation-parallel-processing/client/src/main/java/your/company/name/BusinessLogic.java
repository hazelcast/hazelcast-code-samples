package your.company.name;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * <p>A 4 step process to calculate the <i>Standard Deviation</i>.
 * </p>
 * <p>For steps 1 and 2 we have the option to do the work locally
 * (in this JVM) or remotely (in the Hazelcast cluster). So we do
 * it both ways.
 * </p>
 * <p>We use the class {@link JuniorDeveloper} for the local way,
 * to imply it's not as good. The class {@link SeniorDeveloper}
 * does the remote way, with the implication it is more sophisticated
 * and therefore better. In fact, the skill of the senior
 * developer comes in knowing what ways are available and selecting
 * the most appropriate.
 * </p>
 * <p>Neither method here is perfect. Step 3 divides by the number
 * of entries, which could be zero.
 * </p>
 */
public class BusinessLogic {

    public static void calculate(IMap<Integer, Customer> iMap, HazelcastInstance hazelcastInstance) throws Exception {

        /* Step 1: find the average satisfaction level
         */
        System.out.println("Step 1 : ----------------");

        double localAverage = JuniorDeveloper.average(iMap);
        double remoteAverage = SeniorDeveloper.average(iMap);

        System.out.println("Locally calculated average..: " + localAverage);
        System.out.println("Remotely calculated average.: " + remoteAverage);
        System.out.println("-------------------------");

        /* Step 2: sum the square of the differences from
         * the average satisfaction level
         */
        System.out.println("Step 2 : ----------------");

        double localTotalDifferenceSquared =
                JuniorDeveloper.totalDifferenceSquared(iMap, localAverage);
        double remoteTotalDifferenceSquared =
                SeniorDeveloper.totalDifferenceSquared(iMap, remoteAverage, hazelcastInstance);

        System.out.println("Locally calculated total difference squared..: "
                + localTotalDifferenceSquared);
        System.out.println("Remotely calculated total difference squared.: "
                + remoteTotalDifferenceSquared);
        System.out.println("-------------------------");

        /* Step 3: find the average value from step 2
         */
        System.out.println("Step 3 : ----------------");

        int count = iMap.size();

        double localAverageDifferenceSquared =
                localTotalDifferenceSquared / count;
        double remoteAverageDifferenceSquared =
                remoteTotalDifferenceSquared / count;

        System.out.println("Locally calculated average difference squared..: "
                + localAverageDifferenceSquared);
        System.out.println("Remotely calculated average difference squared.: "
                + remoteAverageDifferenceSquared);
        System.out.println("-------------------------");

        /* Step 4: take the square root from step 3
         */
        System.out.println("Step 4 : ----------------");

        double localStandardDeviation =
                Math.sqrt(localAverageDifferenceSquared);
        double remoteStandardDeviation =
                Math.sqrt(remoteAverageDifferenceSquared);

        System.out.println("Locally calculated STANDARD DEVIATION..: "
                + localStandardDeviation);
        System.out.println("Remotely calculated STANDARD DEVIATION.: "
                + remoteStandardDeviation);
        System.out.println("-------------------------");
    }
}
