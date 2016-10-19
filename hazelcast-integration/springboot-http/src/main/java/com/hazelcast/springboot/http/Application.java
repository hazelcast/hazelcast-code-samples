package com.hazelcast.springboot.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Create a main class tagged for Spring Boot handling.
 *
 * <h3>Usage</h3>
 * When built using "{@code mvn package}" the <i>Jar</i> file created
 * is executable, and can be run with
 * <pre>java -jar springboot-http-0.1-SNAPSHOT.jar</pre>
 *
 * When started, a Tomcat instance is started within the process,
 * along with the web application, and (optionally) a Hazelcast
 * server process. In other words, everything in one, no need to
 * deploy a <i>WAR</i> file to an existing Tomcat, JBoss or similar.
 *
 * The configuration of this application is to start the embedded
 * Tomcat using any available port rather than the default 8080 which
 * is frequently already in use. The port that is picked is clearly
 * shown in the logs, in a line such as
 * "{@code Tomcat started on port(s): 62578 (http)}"
 *
 * Alternatively, you can specify the port to be used on the
 * command line with
 * <pre>java <b>-Dserver.port=8081</b> -jar springboot-http-0.1-SNAPSHOT.jar</pre>
 * if a degree of control is useful.
 *
 * <h3>Sessions</h3>
 * The purpose of this application is to demonstrate the caching of
 * HTTP sessions in Hazelcast, and this is controlled by the compile
 * time constant {@link #USE_HAZELCAST}.
 *
 * When set to {@code false}, Hazelcast isn't started in this process,
 * and normal Tomcat session caching applies. Meaning, when the process
 * is killed the sessions are lost.
 *
 * When set to {@code true}, Hazelcast is started and configured to
 * share the storage of the sessions with any other Hazelcast instances
 * it can find, so that sessions aren't lost if this process is killed.
 * For this to work, obviously other Hazelcast servers need to be running.
 * The easiest way to achieve this is to run another instance of this
 * <i>JAR</i> file -- which is why the port can't be preset or they
 * will clash on the same machine.
 */
@SpringBootApplication
public class Application {

    /**
     * Set to {@code true} to use Hazelcast for distributed session
     * storage. Or, {@code false} to turn off Hazelcast and use the
     * default Tomcat non-distributed implementation.
     *
     * This is a constant rather than a parameter to ensure all
     * instances run with the same setting.
     */
    public static final String USE_HAZELCAST = "true";


    /**
     * Run the application under Spring Boot control.
     *
     * @param args From the command line
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
