## ABOUT
This project is prepared to serve as a sample application for Hazelcast Enterprise.
Here, Hazelcast's use case is Enterprise WAN Batch Replication. 
 
### Scenario
Very briefly, you can send map or cache replication events via WAN Replication.
In this project we have two clusters (clusterA and clusterB) and clusterA replicates events to
clusterB. Also there is a command line interface that you can put objects from clusterA and check the objects in clusterB.

In this project there are two sample applications. One of them is for Map Replication and the other one is for Cache Replication.

## How to Run Sample Application
First you need to set your licence keys.

- EnterpriseMapWanReplicationClusterA reads licence key from hazelcast.xml under resources folder. Simply, change `<license-key>YOUR_LICENSE_KEY</license-key>` with yours. 
- For EnterpriseMapWanReplicationClusterB, EnterpriseCacheWanReplicationClusterA and EnterpriseCacheWanReplicationClusterB, change the value of `static String licenseKey = "YOUR_LICENSE_KEY";`

### Map WAN Replication
For running ClusterA run maven command:
  
`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.map.EnterpriseMapWanReplicationClusterA`

For running ClusterB run maven command:
  
`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.map.EnterpriseMapWanReplicationClusterB`

### Cache WAN Replication
For running ClusterA run maven command:

`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.cache.EnterpriseCacheWanReplicationClusterA`

For running ClusterB run maven command:
  
`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.cache.EnterpriseCacheWanReplicationClusterB`

Example Use Case:  

1) start clusterA:  
`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.map.EnterpriseMapWanReplicationClusterA`
2) start clusterB:  
`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.batch.map.EnterpriseMapWanReplicationClusterB`
3) in clusterA terminal: `put 1 2`
4) in clusterB terminal: `get 1`
5) in clusterA terminal: `putmany 1000`
6) in clusterB terminal: `size` and size should be 1001

You can find the XML configuration `hazelcast.xml` in the `resources` folder.

#### Notes:
In the samples we used the batching WAN publisher that Hazelcast Enterprise is shipped with.
You can implement your own WAN replication by writing a WAN publisher and a WAN consumer class that implement `com.hazelcast.wan.WanPublisher` and `com.hazelcast.wan.WanConsumer` 
respectively. See `<custom-publisher>` and `<consumer>` in `hazelcast.xml` for the configuration options.
