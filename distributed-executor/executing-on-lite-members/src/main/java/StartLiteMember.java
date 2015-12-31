import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class StartLiteMember {
    public static void main(String[] args) {
        final Config config = new Config();
        config.setLiteMember(true);
        Hazelcast.newHazelcastInstance(config);
    }
}
