import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        FlakeIdGenerator idGenerator = hz.getFlakeIdGenerator("idGenerator");
        IMap map = hz.getMap("somemap-" + idGenerator.newId());
    }
}
