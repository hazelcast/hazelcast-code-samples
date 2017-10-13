package com.hazelcast.samples.querying;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * XXX <a href="https://projects.spring.io/spring-shell">Spring Shell</a>
 */
@SpringBootApplication
public class Application {

	/**
	 * XXX
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("hazelcast.logging.type", "slf4j");
		Bootstrap.main(args);
	}	

}
