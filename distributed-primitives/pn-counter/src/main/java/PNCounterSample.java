import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.PNCounterConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.crdt.pncounter.PNCounter;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;


/**
 * Test will add 100 to the PN counter 10 times, wait for 60 seconds and
 * print the value of the counter. If you run this multiple times and have
 * the instances form a cluster, the updates from multiple clients will
 * eventually converge on the final value.
 */
public class PNCounterSample {
    public static void main(String[] args) {
        final Config instanceConfig = new Config();
        instanceConfig.addPNCounterConfig(new PNCounterConfig()
                .setName("pnCounter")
                .setReplicaCount(2)
                .setStatisticsEnabled(true));
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(instanceConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        final PNCounter counter = client.getPNCounter("counter");

        for (int i = 0; i < 10; i++) {
            System.out.println("Value: " + counter.addAndGet(100));
            sleepSeconds(1);
        }
        sleepSeconds(60);
        System.out.println("Final value: " + counter.get());
    }
}
