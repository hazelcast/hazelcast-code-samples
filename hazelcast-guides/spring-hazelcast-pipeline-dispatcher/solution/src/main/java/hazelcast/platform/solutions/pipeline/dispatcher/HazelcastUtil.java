package hazelcast.platform.solutions.pipeline.dispatcher;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastUtil {
    /**
     * Creates an embedded or client HazelcastInstance using the default Hazelcast bootstrapping mechanisms.
     * See https://docs.hazelcast.com/hazelcast/5.2/configuration/understanding-configuration#configuration-precedence
     * for embedded configuration and https://docs.hazelcast.com/hazelcast/5.2/clients/java#configuring-java-client
     * for client configuration.
     *
     * @param embedded
     * @return the HazelcastInstance
     */
    public static HazelcastInstance buildHazelcastInstance( boolean embedded){
        return embedded ?  Hazelcast.newHazelcastInstance(): HazelcastClient.newHazelcastClient();
    }
}
