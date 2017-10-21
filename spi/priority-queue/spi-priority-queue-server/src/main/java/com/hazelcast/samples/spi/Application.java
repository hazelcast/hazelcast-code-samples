package com.hazelcast.samples.spi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Spring will run Hazelcast as a server for us.
 * All the priority queue interaction happens from
 * the client.
 * </p>
 */
@SpringBootApplication
public class Application {

	/**
	 * <p>Start Spring. Indirectly, a 
	 * Hazelcast server instance is created in
	 * this JVM by {@link com.hazelcast.samples.spi.ApplicationConfig ApplicationConfig}.
	 * As the Hazelcast instance won't end
	 * unless signalled to, {@code main()}
	 * won't return and the JVM won't shut down.
	 * </p>
	 * 
	 * @param args From command line
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
