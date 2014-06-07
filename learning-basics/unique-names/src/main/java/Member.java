import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IdGenerator idGenerator = hz.getIdGenerator("idGenerator");
        IMap map = hz.getMap("somemap-" + idGenerator.newId());
    }
}
