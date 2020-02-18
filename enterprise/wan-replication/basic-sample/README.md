## ABOUT
This project is prepared to serve as the most basic sample application for the WAN replication implementation built in
Hazelcast Enterprise. The project configures WAN source and target members with either
XML, YAML or programmatic configuration. All source- and target-side configurations are identical, so a source cluster member
configured with XML configuration can connect to a target configured with programmatic or YAML configuration.
 
## The Scenario
There are two clusters, source and target formed by the members started with `WanSource` and `WanTarget` main classes
respectively. Both clusters print the size of the map named `wan-replicated-map` in every second, but only the source cluster
writes the map, it puts 10 new entries every second. If the `WanTarget` main class is started first, it prints 0 every second.
After starting the `WanSource` cluster, eventually the source and the target cluster members start printing the same size
for the map meaning the entries are replicated from the source cluster to the target cluster.   
 
## Running the Sample Applications
First, a configuration way should be chosen both for the source and the target clusters. By default, both clusters use
programmatic configuration. Switching between the configuration methods can be done by keeping the line performs the chosen
configuration method uncommented in `WanSource` and `WanTarget` classes as shown below:
```java
//        HazelcastInstance hz = instanceConfiguredWithXml();
//        HazelcastInstance hz = instanceConfiguredWithYaml();
HazelcastInstance hz = instanceConfiguredProgrammatically();
```   

Then, your Hazelcast Enterprise license key needs to be set in the configuration. This can be done in the following places.
###### WAN Source Cluster

- XML: hazelcast-wan-source.xml
- YAML: hazelcast-wan-source.yaml
- Programmatic: com.hazelcast.wan.WanSource.LICENSE_KEY constant

###### WAN Target Cluster

- XML: hazelcast-wan-target.xml
- YAML: hazelcast-wan-target.yaml
- Programmatic: com.hazelcast.wan.WanTarget.LICENSE_KEY constant

After the license key is set, the source and target cluster members can be started with the following maven commands:
- target cluster: `mvn exec:java -Dexec.mainClass=com.hazelcast.wan.WanTarget`  
- source cluster: `mvn exec:java -Dexec.mainClass=com.hazelcast.wan.WanSource`

The order of starting the cluster members doesn't matter, the source cluster retains the WAN events in its WAN queue up to 10000
events by default.
