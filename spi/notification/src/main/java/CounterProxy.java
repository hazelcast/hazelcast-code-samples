import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;

import java.util.concurrent.Future;

public class CounterProxy implements Counter {

    private final NodeEngine nodeEngine;
    private final String objectId;

    CounterProxy(String objectId, NodeEngine nodeEngine) {
        this.nodeEngine = nodeEngine;
        this.objectId = objectId;
    }

    @Override
    public String getPartitionKey() {
        return null;
    }

    @Override
    public String getServiceName() {
        return CounterService.NAME;
    }

    @Override
    public String getName() {
        return objectId;
    }

    @Override
    public void await(int value) throws InterruptedException {
        AwaitOperation operation = new AwaitOperation(objectId, value);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(objectId);
        InvocationBuilder builder = nodeEngine.getOperationService()
                .createInvocationBuilder(CounterService.NAME, operation, partitionId);
        try {
            Future future = builder.invoke();
            future.get();
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
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
