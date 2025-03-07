package hazelcast.platform.labs.payments.domain;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;

import java.util.Map;

/*
 * This EntryProcessor is used to process an authorization request.  The code will
 * be executed on the node where the relevant Card entry lives.  Note that processing
 * the authorization can result in updates to the Card entry.
 *
 * EntryProcessors are a Hazelcast mechanism for performing read/update operations safely,
 * without the need for holding a lock or other concurrency management techniques.
 * See https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/map/EntryProcessor.html
 * for more.
 */
public class TransactionEntryProcessor implements EntryProcessor<String, GenericRecord, String> {
    public TransactionEntryProcessor(Transaction t) {
        this.transaction = t;
    }
    private final Transaction transaction;
    @Override
    public String process(Map.Entry<String, GenericRecord> entry) {
        GenericRecord card = entry.getValue();
        if (card == null)
            return Transaction.Status.INVALID_CARD.name();


//
//        Add the big transaction check and the card locked check by
//        un-commenting these lines
//
//        if (transaction.getAmount() > 5000)
//            return Transaction.Status.DECLINED_BIG_TXN.name();
//
//        boolean locked = card.getBoolean("locked");
//        if (locked)
//            return Transaction.Status.DECLINED_LOCKED.name();

        int authorizedDollars = card.getInt32("authorizedDollars");
        int creditLimitDollars = card.getInt32("creditLimitDollars");

        if (authorizedDollars + transaction.getAmount() <= creditLimitDollars){
            GenericRecord updatedCard = card.newBuilderWithClone()
                    .setInt32("authorizedDollars", authorizedDollars + transaction.getAmount()).build();
            entry.setValue(updatedCard);
            return Transaction.Status.APPROVED.name();
        } else {
            return Transaction.Status.DECLINED_OVER_AUTH_LIMIT.name();
        }
    }
}
