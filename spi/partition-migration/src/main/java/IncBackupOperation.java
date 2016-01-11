import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.AbstractOperation;
import com.hazelcast.spi.BackupOperation;

import java.io.IOException;

@SuppressWarnings("unused")
class IncBackupOperation extends AbstractOperation implements BackupOperation {

    private String objectId;
    private int amount;

    public IncBackupOperation() {
    }

    IncBackupOperation(String objectId, int amount) {
        this.amount = amount;
        this.objectId = objectId;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(objectId);
        out.writeInt(amount);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        objectId = in.readUTF();
        amount = in.readInt();
    }

    @Override
    public void run() throws Exception {
        CounterService service = getService();
        System.out.println("Executing backup " + objectId + ".inc() on: " + getNodeEngine().getThisAddress());
        Container container = service.containers[getPartitionId()];
        container.inc(objectId, amount);
    }
}
