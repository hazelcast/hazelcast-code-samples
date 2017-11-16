package com.hazelcast.samples.querying;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * <P>
 * Run an application via
 * <a href="https://projects.spring.io/spring-shell">Spring Shell</a> which will
 * provide a command line interface with some built-in commands plus extras
 * defined in {@link com.hazelcast.samples.querying.MyCommands MyCommands} and
 * {@link com.hazelcast.samples.querying.testdata.TestDataLoader TestDataLoader}
 * </P>
 */
@SpringBootApplication
public class Application {

    /**
     * <P>
     * As not using {@code SpringApplication.run()}, need to set logging type
     * manually.
     * </P>
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Bootstrap.main(args);
    }

}
