import com.hazelcast.config.Config;
import com.hazelcast.config.EndpointConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.config.WanBatchPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanReplicationRef;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Collections;
import java.util.Map;

public class MemberA {

    public static void main(String[] args) {
        Config config = new Config();

        // AdvancedNetworkConfig works similarly to NetworkConfig however
        // allows finer control of network configuration
        // In order to use AdvancedNetworkConfig, it must be explicitly enabled
        config.getAdvancedNetworkConfig().setEnabled(true);

        // configure cluster joiner: disable default multicast joiner, enable & configure TCP/IP
        config.getAdvancedNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getAdvancedNetworkConfig().getJoin()
              .getTcpIpConfig().setEnabled(true).addMember("192.168.1.10,192.168.1.11,192.168.1.12");

        // configure the MEMBER protocol server socket to listen on 192.168.1.10-12:5701
        config.getAdvancedNetworkConfig().setMemberEndpointConfig(createMemberEndpointConfig());

        // configure the CLIENT protocol server socket:
        // - listen on 10.10.200.10-12
        // - advertise public address 172.10.10.10:9000
        config.getAdvancedNetworkConfig().setClientEndpointConfig(createClientEndpointConfig());

        // configure outgoing WAN connections to use SSL
        config.getAdvancedNetworkConfig().addWanEndpointConfig(createWanEndpointConfig());

        // setup WAN replication
        WanBatchPublisherConfig wanBatchReplicationPublisherConfig = new WanBatchPublisherConfig()
                .setClusterName("cluster-b")
                .setClassName("com.hazelcast.enterprise.wan.replication.WanBatchReplication")
                // refer to the WAN endpoint config by name
                .setEndpoint("active-wan");
        Map<String, Comparable> props = wanBatchReplicationPublisherConfig.getProperties();
        props.put("endpoints", "147.102.1.10:8443,147.102.1.11:8443,147.102.1.12:8443");
        props.put("group.password", "clusterB-pass");

        config.addWanReplicationConfig(
                new WanReplicationConfig()
                        .setName("active-wan-replication")
                        .addBatchReplicationPublisherConfig(wanBatchReplicationPublisherConfig));

        config.addMapConfig(
                new MapConfig("wan-map")
                    .setWanReplicationRef(
                        new WanReplicationRef("active-wan-replication",
                                "com.hazelcast.enterprise.wan.replication.WanBatchReplication",
                                Collections.<String>emptyList(),
                                false)));

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
    }

    private static ServerSocketEndpointConfig createMemberEndpointConfig() {
        ServerSocketEndpointConfig endpointConfig = new ServerSocketEndpointConfig();
        // similarly to NetworkConfig, port, auto-increment and other settings can be configured
        // for each individual endpoint config
        endpointConfig.setPort(5701)
                      .setPortAutoIncrement(false)
                      .setReuseAddress(true);

        // enable interfaces
        endpointConfig.getInterfaces().setEnabled(true)
                      .addInterface("192.168.1.10-12");

        return endpointConfig;
    }

    private static ServerSocketEndpointConfig createClientEndpointConfig() {
        ServerSocketEndpointConfig endpointConfig = new ServerSocketEndpointConfig();
        endpointConfig.setPort(9000)
                      .setPortAutoIncrement(false)
                      .setReuseAddress(true);

        // enable interfaces
        endpointConfig.getInterfaces().setEnabled(true)
                      .addInterface("10.10.200.10-12");

        // set public address of this member for NAT
        endpointConfig.setPublicAddress("172.10.10.10:9000");

        return endpointConfig;
    }

    // We are configuring the active side of WAN replication, therefore no listening
    // socket is required. That's why we configure WAN endpoints with an EndpointConfig
    // instead of a ServerSocketEndpointConfig.
    private static EndpointConfig createWanEndpointConfig() {
        EndpointConfig endpointConfig = new EndpointConfig();
        endpointConfig.setName("active-wan")
                      .setSSLConfig(new SSLConfig()
                              .setEnabled(true)
                              .setProperty("trustStore", "truststore")
                              .setProperty("trustStorePassword", System.getenv("KEYSTORE_PASSWORD")));

        return endpointConfig;
    }
}
