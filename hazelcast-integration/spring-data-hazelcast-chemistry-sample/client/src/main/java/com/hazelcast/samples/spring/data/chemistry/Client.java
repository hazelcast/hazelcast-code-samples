package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <P>Run a Hazelcast client as a REST application. Data can be inspected
 * using simple URL calls in a browser.
 * </P>
 * <P>Use <A HREF="http://localhost:8080/">http://localhost:8080/</A>
 * to discover what URLs are available.
 * </P>
 */
@SpringBootApplication
public class Client {
	
	/**
	 * <P>Launch Spring Boot.
	 * </P>
	 * 
     * @param args		  From the O/s to pass on          
	 */
	public static void main(String[] args) {
		SpringApplication.run(Client.class, args);
	}
	
}
