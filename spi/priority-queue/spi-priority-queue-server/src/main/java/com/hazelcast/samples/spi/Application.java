package com.hazelcast.samples.spi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>A "<i>normal</i>" server in the cluster. You
 * should start at least one of these first. It doesn't
 * do anything except host the data.
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
