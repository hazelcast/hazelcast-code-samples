import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;

public class IdGeneratorMember {

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
        IdGenerator idGenerator = hazelcast.getIdGenerator("idGenerator");
        for (int i = 0; i < 10000; i++) {
            Thread.sleep(1000);
            System.out.printf("Id: %s\n", idGenerator.newId());
        }
    }
}
