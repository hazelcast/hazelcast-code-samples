package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import javax.enterprise.inject.Produces;

@ApplicationPath("application")
@ApplicationScoped
public class MapApplication extends Application {

    @Produces
    HazelcastInstance create() {
        Config config = new Config();
        config.getGroupConfig().setName("hz-microprofile");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getKubernetesConfig().setEnabled(true);
        return Hazelcast.newHazelcastInstance(config);
    }
}