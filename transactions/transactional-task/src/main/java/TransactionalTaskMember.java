import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionException;
import com.hazelcast.transaction.TransactionalTask;
import com.hazelcast.transaction.TransactionalTaskContext;

public class TransactionalTaskMember {

    public static void main(String[] args) throws Throwable {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        hz.executeTransaction(new TransactionalTask() {
            @Override
            public Object execute(TransactionalTaskContext context) throws TransactionException {
                TransactionalMap<String, String> map = context.getMap("map");
                map.put("1", "1");
                map.put("2", "2");
                return null;
            }
        });

        System.out.println("Finished");
        Hazelcast.shutdownAll();
    }
}
