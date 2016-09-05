package com.hazelcast.springboot.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <P>Create a main class tagged for Spring Boot handling.
 * </P>
 * <H3>Usage</H3>
 * <P>When built using "{@code mvn package}" the <I>Jar</I> file created
 * is executable, and can be run with
 * <PRE>java -jar springboot-http-0.1-SNAPSHOT.jar</PRE>
 * </P>
 * <P>When started, a Tomcat instance is started within the process,
 * along with the web application, and (optionally) a Hazelcast
 * server process. In otherwords, everything in one, no need to
 * deploy a <I>War</I> file to an existing Tomcat, JBoss or similar.
 * </P>
 * <P>The configuration of this application is to start the embedded
 * Tomcat using any available port rather than the default 8080 which
 * is frequently already in use. The port that is picked is clearly
 * shown in the logs, in a line such as 
 * "{@code Tomcat started on port(s): 62578 (http)}"
 * </P>
 * <P>Alternatively, you can specify the port to be used on the
 * command line with
 * <PRE>java <B>-Dserver.port=8081</B> -jar springboot-http-0.1-SNAPSHOT.jar</PRE>
 * if a degree of control is useful.
 * </P>
 * <H3>Sessions</H3>
 * <P>The purpose of this application is to demonstrate the caching of
 * HTTP sessions in Hazelcast, and this is controlled by the compile
 * time constant {@link #USE_HAZELCAST}.
 * </P>
 * <P>When set to {@code false}, Hazelcast isn't started in this process,
 * and normal Tomcat session caching applies. Meaning, when the process
 * is killed the sessions are lost.
 * </P>
 * <P>When set to {@code true}, Hazelcast is started and configured to
 * share the storage of the sessions with any other Hazelcast instances
 * it can find, so that sessions aren't lost if this process is killed.
 * For this to work, obviously other Hazelcast servers need to be running.
 * The easiest way to achieve this is to run another instance of this
 * <I>Jar</I> file -- which is why the port can't be preset or they
 * will clash on the same machine.
 * </P>
 */
@SpringBootApplication
public class Application {

	/**
	 * <P>Set to {@code true} to use Hazelcast for distributed session
	 * storage. Or, {@code false} to turn off Hazelcast and use the
	 * default Tomcat non-distributed implementation.
	 * </P>
	 * <P>This is a constant rather than a parameter to ensure all
	 * instances run with the same setting.
	 * </P>
	 */
	public static final String USE_HAZELCAST = "true";
	

	/**
	 * <P>Run the application under Spring Boot control.
	 * </P>
	 * 
	 * @param args From the command line
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
