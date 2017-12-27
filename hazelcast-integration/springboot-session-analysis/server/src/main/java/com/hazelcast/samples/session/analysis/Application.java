package com.hazelcast.samples.session.analysis;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * <p>Kick off <a href="https://projects.spring.io/spring-shell/">Spring Shell</a>
 * to run things on our behalf.
 * </p>
 */
@SpringBootApplication
public class Application {

    /**
     * <p>Direct Hazelcast to use the same logging as Spring.
     * </p>
     *
     * @param args From the command line
     * @throws Exception Hopefully not
     */
    public static void main(String[] args) throws Exception {

            System.setProperty("hazelcast.logging.type", "slf4j");
        System.setProperty("my.group.name", Constants.MY_GROUP_NAME);
        System.setProperty("my.group.password", Constants.MY_GROUP_PASSWORD);

        Bootstrap.main(args);
    }

}
