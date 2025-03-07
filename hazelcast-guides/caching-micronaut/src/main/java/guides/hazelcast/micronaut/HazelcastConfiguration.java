package guides.hazelcast.micronaut;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class HazelcastConfiguration {

    @Bean
    public Config hazelcastConfig() {
        Config configuration = new Config()
          .setClusterName("micronaut-cluster");
        JoinConfig joinConfig = configuration.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true).addMember("localhost");
        return configuration;
    }
}
