import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class PromoteLiteMember {

    public static void main(String[] args) {
        Config config = new Config();
        config.setLiteMember(true);

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        Cluster cluster = hazelcastInstance.getCluster();
        cluster.promoteLocalLiteMember();
    }
}
