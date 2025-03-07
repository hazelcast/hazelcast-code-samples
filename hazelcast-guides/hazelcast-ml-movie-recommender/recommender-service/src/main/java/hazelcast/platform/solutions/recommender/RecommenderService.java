package hazelcast.platform.solutions.recommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "hazelcast.platform.solutions")
public class RecommenderService {
	public static void main(String[] args) {
		SpringApplication.run(RecommenderService.class, args);
	}

}
