import com.hazelcast.core.PartitionAware;

import java.io.Serializable;

public final class OrderKey implements PartitionAware, Serializable {
    public final long orderId;
    public final long customerId;

    public OrderKey(long orderId, long customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }

    public Object getPartitionKey() {
        return customerId;
    }
}