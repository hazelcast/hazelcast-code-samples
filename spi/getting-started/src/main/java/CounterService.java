import com.hazelcast.internal.services.ManagedService;
import com.hazelcast.spi.impl.NodeEngine;

import java.util.Properties;

@SuppressWarnings("unused")
public class CounterService implements ManagedService {

    private NodeEngine nodeEngine;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        System.out.println("CounterService.init()");
        this.nodeEngine = nodeEngine;
    }

    @Override
    public void shutdown(boolean terminate) {
        System.out.println("CounterService.shutdown()");
    }

    @Override
    public void reset() {
    }

    public NodeEngine getNodeEngine() {
        return nodeEngine;
    }
}
