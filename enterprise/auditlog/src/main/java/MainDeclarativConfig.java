import com.hazelcast.core.Hazelcast;

/**
 * This program prints the auditable events to a file {@code auditlog-json.log}. The custom auditlog is configured in the
 * {@code hazelcast.yml} file.
 */
public class MainDeclarativConfig {

    public static void main(String[] args) {
        Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();
        Hazelcast.shutdownAll();
    }
}
