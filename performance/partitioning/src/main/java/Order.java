import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

@SuppressWarnings("unused")
class Order implements DataSerializable {

    private String orderId;
    private String customerId;
    private int amount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(orderId);
        out.writeUTF(customerId);
        out.write(amount);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        orderId = in.readUTF();
        customerId = in.readUTF();
        amount = in.readInt();
    }
}
