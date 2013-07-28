import com.hazelcast.spi.Invocation;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DistributedCounterProxy implements Counter {
    private final NodeEngine nodeEngine;
    private final String objectId;

    public DistributedCounterProxy(String objectId, NodeEngine nodeEngine) {
        this.nodeEngine = nodeEngine;
        this.objectId = objectId;
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
    public int inc(int amount) {
        IncOperation operation = new IncOperation(objectId, amount);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(objectId);
        InvocationBuilder builder = nodeEngine.getOperationService()
                .createInvocationBuilder(CounterService.NAME, operation, partitionId);
        Invocation invocation = builder.build();
        try {
            Future<Integer> future = invocation.invoke();
            return future.get();
        } catch(Exception e){
            throw ExceptionUtil.rethrow(e);
        }
    }

    @Override
    public void destroy() {
    }
}
