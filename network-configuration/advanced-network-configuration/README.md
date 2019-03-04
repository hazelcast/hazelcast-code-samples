Advanced Network Configuration
==============================

Starting with Hazelcast 3.12, it is possible to configure the Hazelcast members with separate server sockets using a different network configuration for different protocols. This configuration scheme allows more flexibility when deploying Hazelcast.

In this example, Hazelcast members & clients are configured assuming the following deployment scenario:

 - Cluster A members have two network interfaces configured as follows:
   - one network interface has IPv4 addresses in `192.168.1.10-12` range. Member-to-member communication and
   REST API for cluster management bind to this interface.
   - second network interface has IPv4 addresses in `10.10.200.10-12` addresses. These are translated 
   to addresses in range `172.10.10.10-12` for clients to connect to.
 - Additionally, Cluster A members are configured to replicate to Cluster B at addresses `147.102.1.10-12`.
 Since WAN communication traverses public networks, it is configured with SSL encryption enabled.
 - Cluster A clients are configured to connect to cluster members at addresses `172.10.10.10-12`.
 - Cluster B members are configured to listen for incoming WAN connections on port 8443.
 
```
 +---------------------- Location 1 --------------------------------------------+
 |  Cluster A                                                                   |
 |                                                                              |
 |     Internal members network               |  Hazelcast clients network      |
 |                                            |                                 |
 |      Member 1                              |                                 |
 |  +--------------+                          |  +-----------------+            |
 |  | 192.168.1.10 |                          |  | cluster members |            |
 |  |              |                          |  |   addressed as  |            |
 |  | 10.10.200.10 +----+-- router/firewall --+- | 172.10.10.10-12 |            |
 |  +--------------+    |                     |  +-----------------+            |
 |                      |                     |                                 |
 |      Member 2        |                     |                                 |
 |  +--------------+    |                     |                                 |
 |  | 192.168.1.11 |    |                     |                                 |
 |  |              |    |                     |                                 |
 |  | 10.10.200.11 +----+                     |                                 |
 |  +--------------+                          |                                 |
 |                                                                              |
 |      ...                                                                     |
 |                                                                              |
 +-----+------------------------------------------------------------------------+
       |
       |
       |
       |
 +-----+---------------- Location 2 --------------------------------------------+
 |     |                                                                        |
 |   WAN replication target cluster members (147.102.1.10-12)                   |
 |                                                                              |
 +------------------------------------------------------------------------------+

``` 
 
 - `MemberA.java` configures a member of Cluster A with network interfaces `192.168.1.10` and `10.10.200.10`:
   - using TCP/IP joiner with `192.168.1.10-12` as member addresses
   - to listen on `192.168.1.10:5701` for member protocol connections
   - to listen on `10.10.200.10:9000` for client connections, while setting this endpoint's public address to `172.10.10.10:9000`
   - WAN replication for map is configured with SSL enabled
   
 - `ClientA.java` configures a client to connect to cluster A members. The member addresses known to clients are `172.10.10.10-12`, so these are the ones configured for clients to connect to. 
 
 - `MemberB.java` is configured with the default member protocol server socket (without explicit configuration) and a WAN server socket listening for incoming connections on port 8443 with SSL configuration enabled. 


Notes
=====================
 - This sample cannot be executed due to usage of specific IP addresses in configuration. It is only meant to illustrate usage of advanced network configuration.
 - Some features configured in this sample (WAN replication, SSL) require a Hazelcast Enterprise license.
