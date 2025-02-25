package guides.hazelcast.tomcatsessionmanager;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.session.HazelcastSessionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setInstanceName("hazelcastInstance");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true).addMember("localhost");
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return Hazelcast.getOrCreateHazelcastInstance(hazelcastConfig);
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizeTomcat(HazelcastInstance hazelcastInstance) {
        return (factory) -> {
            factory.addContextCustomizers(context -> {
                HazelcastSessionManager manager = new HazelcastSessionManager();
                manager.setSticky(false);
                manager.setHazelcastInstanceName("hazelcastInstance");
                context.setManager(manager);
            });
        };
    }

    @RequestMapping("/")
    public String homepage(){
        return "Homepage\n";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
