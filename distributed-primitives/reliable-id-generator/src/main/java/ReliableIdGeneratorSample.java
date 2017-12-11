import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.ReliableIdGeneratorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReliableIdGenerator;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;
import static java.util.concurrent.TimeUnit.MINUTES;

public class ReliableIdGeneratorSample {
    public static void main(String[] args) {
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();

        ClientConfig clientConfig = new ClientConfig()
                .addReliableIdGeneratorConfig(new ReliableIdGeneratorConfig("idGenerator")
                        .setPrefetchCount(10)
                        .setPrefetchValidityMillis(MINUTES.toMillis(10)));
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        ReliableIdGenerator idGenerator = client.getReliableIdGenerator("idGenerator");
        for (int i = 0; i < 10000; i++) {
            sleepSeconds(1);
            System.out.printf("Id: %s\n", idGenerator.newId());
        }
    }
}
