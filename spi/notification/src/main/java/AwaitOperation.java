import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

class AwaitOperation extends Operation {

    private String objectId;
    private int amount;

    @SuppressWarnings("unused")
    public AwaitOperation() {
    }

    AwaitOperation(String objectId, int amount) {
        this.amount = amount;
        this.objectId = objectId;
    }

    @Override
    public void run() {
        System.out.println("Executing " + objectId + ".await() on: " + getNodeEngine().getThisAddress());

        CounterService service = getService();
        Container container = service.containers[getPartitionId()];
        container.await(objectId, amount);
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
}
