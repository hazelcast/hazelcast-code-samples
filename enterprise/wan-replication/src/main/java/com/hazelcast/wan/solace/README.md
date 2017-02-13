<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast Enterprise
Here, Hazelcast's use case is Enterprise Wan Replication using Solace Message Routers. 
 
<h3>Scenario</h3>
Very briefly, you can send map or cache replication events via WAN Replication.
In this project we have two clusters (clusterA and clusterB) and clusterA publishes replication events to Solace message router.
Each partition publishes its events to its specific queue named T/hz/clusterA/[partition-id].
Messages from these topics are gathered in a single queue, named SampleQueue, by using topic-to-queue bridging. 
clusterB consumes the events from the SampleQueue.
Also there is a command line interface that you can put objects from clusterA and check the objects in clusterB.

<h2>How to Run Sample Application</h2>
First you need to set your licence keys.

- SolaceWanReplicationClusterA reads license key from hazelcast-solace-clusterA.xml under resources folder. Simply, change `<license-key>YOUR_LICENSE_KEY</license-key>` with yours. 
- SolaceWanReplicationClusterB reads license key from hazelcast-solace-clusterB.xml under resources folder. Simply, change `<license-key>YOUR_LICENSE_KEY</license-key>` with yours.

Then define the host, username and password properties required to connect your Solace appliance.

<h3>Starting Clusters</h3>
For running ClusterA:
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.solace.SolaceWanReplicationClusterA`

For running ClusterB:
1) run maven command: <br/>`mvn exec:java -Dexec.mainClass=com.hazelcast.wan.solace.SolaceWanReplicationClusterB`


Example Use Case:<br/>
1) start clusterA:<br/> `mvn exec:java -Dexec.mainClass=com.hazelcast.wan.solace.SolaceWanReplicationClusterA`<br/>
2) start clusterB:<br/> `mvn exec:java -Dexec.mainClass=com.hazelcast.wan.solace.SolaceWanReplicationClusterB`<br/>
3) in clusterA terminal: `put 1 2`<br/>
4) in clusterB terminal: `get 1`<br/>
5) in clusterA terminal: `putmany 1000`<br/>
6) in clusterB termibal: `size`  and size should be 1001<br/>
