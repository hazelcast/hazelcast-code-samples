import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Customer implements DataSerializable {

    private String customerId;

    String getCustomerId() {
        return customerId;
    }

    void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(customerId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        customerId = in.readUTF();
    }
}
