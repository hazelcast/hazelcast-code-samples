import com.hazelcast.cardinality.CardinalityEstimator;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.String.format;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        CardinalityEstimator visitorsEstimator = hz.getCardinalityEstimator("visitors");

        InputStreamReader isr = new InputStreamReader(Member.class.getResourceAsStream("visitors.txt"));
        BufferedReader br = new BufferedReader(isr);
        try {

            for(String visitor; (visitor = br.readLine()) != null; ) {
                visitorsEstimator.add(visitor);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(format("Estimated unique visitors seen so far %s", visitorsEstimator.estimate()));

        Hazelcast.shutdownAll();
    }
}
