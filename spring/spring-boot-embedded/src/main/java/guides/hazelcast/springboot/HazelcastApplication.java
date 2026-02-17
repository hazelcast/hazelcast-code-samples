package guides.hazelcast.springboot;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(excludeName = "com.hazelcast.spring.HazelcastObjectExtractionConfiguration")
public class HazelcastApplication {

    public static void main(String[] args) {
        SpringApplication.run(HazelcastApplication.class, args);
    }

    //tag::imap-bean[]
    @Bean
    public IMap<String, String> map(@Autowired HazelcastInstance hazelcast) {
        return hazelcast.getMap("map");
    }
    //end::imap-bean[]
}
