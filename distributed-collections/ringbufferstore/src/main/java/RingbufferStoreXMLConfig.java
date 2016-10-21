import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class RingbufferStoreXMLConfig {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        hz.getRingbuffer("object-ringbuffer-xml").add(new Item());
        hz.getRingbuffer("binary-ringbuffer-xml").add(new Item());

        System.exit(0);
    }
}
