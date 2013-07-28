import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;

import java.util.Properties;

public class CounterService implements ManagedService {
    public static final String NAME = "CounterService";

    private NodeEngine nodeEngine;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        System.out.println("CounterService.init");
        this.nodeEngine = nodeEngine;
    }

    @Override
    public String getServiceName() {
        return NAME;
    }

    @Override
    public void shutdown() {
        System.out.println("CounterService.shutdown");
    }

    @Override
    public void reset() {
    }
}
