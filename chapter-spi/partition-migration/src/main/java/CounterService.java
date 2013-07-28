import com.hazelcast.core.DistributedObject;
import com.hazelcast.partition.MigrationEndpoint;
import com.hazelcast.spi.*;

import java.util.Map;
import java.util.Properties;

public class CounterService implements ManagedService, RemoteService, MigrationAwareService {
    public final static String NAME = "CounterService";

    private NodeEngine nodeEngine;
    Container[] containers;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        containers = new Container[nodeEngine.getPartitionService().getPartitionCount()];
        for (int k = 0; k < containers.length; k++)
            containers[k] = new Container();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public DistributedObject createDistributedObject(Object objectId) {
        return new DistributedCounterProxy(String.valueOf(objectId), nodeEngine);
    }

    @Override
    public String getServiceName() {
        return NAME;
    }

    @Override
    public void beforeMigration(PartitionMigrationEvent e) {
        //no-op
    }

    @Override
    public void clearPartitionReplica(int partitionId) {
        //todo:
    }

    @Override
    public Operation prepareReplicationOperation(PartitionReplicationEvent event) {
        //todo:
        return null;
    }

    @Override
    public Operation prepareMigrationOperation(PartitionMigrationEvent e) {
        if (e.getReplicaIndex() > 1) return null;

        Container container = containers[e.getPartitionId()];
        Map<String, Integer> migrationData = container.toMigrationData();
        if (migrationData.isEmpty()) return null;
        return new CounterMigrationOperation(migrationData);
    }

    @Override
    public void commitMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.SOURCE
                && e.getMigrationType() == MigrationType.MOVE) {
            Container c = containers[e.getPartitionId()];
            c.clear();
        }
    }

    @Override
    public void rollbackMigration(PartitionMigrationEvent e) {
        if (e.getMigrationEndpoint() == MigrationEndpoint.DESTINATION) {
            Container c = containers[e.getPartitionId()];
            c.clear();
        }
    }

    @Override
    public void destroyDistributedObject(Object objectId) {
    }

    @Override
    public void reset() {
    }
}
