package com.hazelcast.samples.spring.data.migration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.shell.Bootstrap;

/**
 * Run the translater much as before, using the command line to invoke the translation service.
 *
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
 * <li>Duplicate this class from JPA repository, or move to a central module.</li>
 * </ol>
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AfterTranslator {

    public static void main(String[] args) throws Exception {
        Bootstrap.main(args);
    }
}
