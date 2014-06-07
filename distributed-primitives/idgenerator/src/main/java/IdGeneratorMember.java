import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;

public class IdGeneratorMember {
    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
        IdGenerator idGenerator = hazelcast.getIdGenerator("idGenerator");
        for (int k = 0; k < 10000; k++) {
            Thread.sleep(1000);
            System.out.printf("Id : %s\n", idGenerator.newId());
        }
    }
}
