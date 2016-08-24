import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.MigrationAwareService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionMigrationEvent;
import com.hazelcast.spi.PartitionReplicationEvent;
import com.hazelcast.spi.RemoteService;
import com.hazelcast.spi.partition.MigrationEndpoint;

import java.util.Map;
import java.util.Properties;

public class CounterService implements ManagedService, RemoteService, MigrationAwareService {

    static final String NAME = "CounterService";

    Container[] containers;
    private NodeEngine nodeEngine;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        containers = new Container[nodeEngine.getPartitionService().getPartitionCount()];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new Container();
        }
    }

    @Override
    public void shutdown(boolean b) {
    }

    @Override
    public DistributedObject createDistributedObject(String objectId) {
        return new CounterProxy(objectId, nodeEngine);
    }

    @Override
    public void destroyDistributedObject(String s) {
    }

    @Override
    public void beforeMigration(PartitionMigrationEvent partitionMigrationEvent) {
    }

    @Override
    public Operation prepareReplicationOperation(PartitionReplicationEvent e) {
        if (e.getReplicaIndex() > 1) {
            return null;
        }

        Container container = containers[e.getPartitionId()];
        Map<String, Integer> migrationData = container.toMigrationData();
        if (migrationData.isEmpty()) {
            return null;
        }
        return new CounterMigrationOperation(migrationData);
    }

    @Override
    public void commitMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.SOURCE) {
            containers[e.getPartitionId()].clear();
        }
    }

    @Override
    public void rollbackMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.DESTINATION) {
            containers[e.getPartitionId()].clear();
        }
    }

    @Override
    public void reset() {
    }
}
