package com.hazelcast.samples.spring.data.migration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * Use <a href="https://projects.spring.io/spring-shell">Spring Shell</a>
 * for command line handling as it's more appropriate than
 * <a href="http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-remote-shell.html">CRaSH</a>
 * although not as nicely integrated yet.
 *
 * @see <a href="https://github.com/spring-projects/spring-shell/issues/34">
 * https://github.com/spring-projects/spring-shell/issues/34</a>
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>This is a new module, but this class is just a clone of the other main classes.</li>
 * </ol>
 */
@SpringBootApplication
public class HazelcastServer {

    public static void main(String[] args) throws Exception {
        Bootstrap.main(args);
    }
}
