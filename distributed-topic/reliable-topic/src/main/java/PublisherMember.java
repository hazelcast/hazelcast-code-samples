import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import java.util.Random;

import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;

public class PublisherMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Random random = new Random();

        ITopic<Long> topic = hz.getReliableTopic("sometopic");
        long messageId = 0;

        while (true) {
            topic.publish(messageId);
            messageId++;

            System.out.println("Written: " + messageId);

            // add a bit of randomization
            sleepMillis(random.nextInt(100));
        }
    }
}
