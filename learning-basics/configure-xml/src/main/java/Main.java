import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;

public class Main {

    public static void main(String[] args) {
        HazelcastInstance member = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        sleepMillis(100);

        client.shutdown();
        member.shutdown();
    }
}
