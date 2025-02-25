package guides.hazelcast.spring;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RestController
public class Application {

    
    @Value("#{environment.MY_POD_NAME}")
    private String podName;

    private static Config config;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setProperty( "hazelcast.logging.type", "slf4j" );
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getKubernetesConfig().setEnabled(true);
        return config;
    }

    @RequestMapping("/")
    public String homepage(){
	return "Homepage hosted by: " + podName + "\n";
    }
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
