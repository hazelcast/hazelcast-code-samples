import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;

import java.util.concurrent.TimeUnit;

public class TransactionalMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        TransactionOptions txOptions = new TransactionOptions().setTimeout(10, TimeUnit.SECONDS);

        TransactionContext txCxt = hz.newTransactionContext(txOptions);

        txCxt.beginTransaction();
        TransactionalMap<String, String> map = txCxt.getMap("map");

        try {
            map.put("1", "1");
            Thread.sleep(TimeUnit.SECONDS.toMillis(20));
            map.put("2", "2");
            txCxt.commitTransaction();
        } catch (RuntimeException t) {
            txCxt.rollbackTransaction();
            throw t;
        }

        System.out.println("Finished");
        Hazelcast.shutdownAll();
    }
}
