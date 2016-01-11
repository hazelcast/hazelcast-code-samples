<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast Enterprise
Here, Hazelcast's use case is Enterprise Wan Replication. 
 
<h3>Scenario</h3>
Very briefly, you can send map or cache replication events via Wan Replication.
In this project we have two clusters (clusterA and clusterB) and clusterA replicates events to
clusterB. Also there is a command line interface that you can put objects from clusterA and check the objects in clusterB.

In this project there are two sample applications. One of them is for Map Replication and the other one is for Cache Replication.

<h2>How to Run Sample Application</h2>
First you need to set your licence keys.

- EnterpriseMapWanReplicationClusterA reads licence key from hazelcast.xml under resources folder. Simply, change `<license-key>YOUR_LICENSE_KEY</license-key>` with yours. 
- For EnterpriseMapWanReplicationClusterB, EnterpriseCacheWanReplicationClusterA and EnterpriseCacheWanReplicationClusterB, change the value of `static String licenseKey = "YOUR_LICENSE_KEY";`

<h3>Map Wan Replication</h3>
For running ClusterA:
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.map.wanreplication.EnterpriseMapWanReplicationClusterA`

For running ClusterB:
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.map.wanreplication.EnterpriseMapWanReplicationClusterB`

<h3>Cache Wan Replication</h3>
For running ClusterA:<br/>
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.cache.wanreplication.EnterpriseCacheWanReplicationClusterA`

For running ClusterB:<br/>
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.cache.wanreplication.EnterpriseCacheWanReplicationClusterB`

Example Use Case:<br/>
1) start clusterA:<br/> `mvn exec:java -Dexec.mainClass=com.hazelcast.map.wanreplication.EnterpriseMapWanReplicationClusterA`<br/>
2) start clusterB:<br/> `mvn exec:java -Dexec.mainClass=com.hazelcast.map.wanreplication.EnterpriseMapWanReplicationClusterB`<br/>
3) in clusterA terminal: `put 1 2`<br/>
4) in clusterB terminal: `get 1`<br/>
5) in clusterA terminal: `putmany 1000`<br/>
6) in clusterB termibal: `size`  and size should be 1001<br/>

You can find xml configuration under map wan replication sample.<br/>

<h4>Notes:</h4>
1) In the samples we used `com.hazelcast.enterprise.wan.replication.WanNoDelayReplication` implementation.<br/>
2) You can change implementation to BatchReplication `com.hazelcast.enterprise.wan.replication.WanBatchReplication`<br/>
3) Also you can enable `snapshot` parameter if you use BatchReplication.<br/>
