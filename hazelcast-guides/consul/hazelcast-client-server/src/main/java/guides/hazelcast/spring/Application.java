package guides.hazelcast.spring;

import com.hazelcast.client.config.ClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public ClientConfig config() {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().getKubernetesConfig().setEnabled(true);
        config.getNetworkConfig().getKubernetesConfig().setProperty("service-name","hazelcast-cluster");
        return config;
    }
}
