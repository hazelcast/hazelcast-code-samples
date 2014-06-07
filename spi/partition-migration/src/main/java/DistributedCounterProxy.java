import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;

import java.util.concurrent.Future;

public class DistributedCounterProxy implements Counter {
    private final NodeEngine nodeEngine;
    private final String objectId;

    public DistributedCounterProxy(String objectId, NodeEngine nodeEngine) {
        this.nodeEngine = nodeEngine;
        this.objectId = objectId;
    }

    @Override
    public String getServiceName() {
        return CounterService.NAME;
    }

    @Override
    public Object getId() {
        return objectId;
    }

    @Override
    public String getName() {
        return objectId;
    }

    @Override
    public String getPartitionKey() {
        throw new RuntimeException("todo");
    }

    @Override
    public int inc(int amount) {
        IncOperation operation = new IncOperation(objectId, amount);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(objectId);
        InvocationBuilder builder = nodeEngine.getOperationService()
                .createInvocationBuilder(CounterService.NAME, operation, partitionId);
        try {
            Future<Integer> future = builder.invoke();
            return future.get();
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    @Override
    public void destroy() {
    }
}
