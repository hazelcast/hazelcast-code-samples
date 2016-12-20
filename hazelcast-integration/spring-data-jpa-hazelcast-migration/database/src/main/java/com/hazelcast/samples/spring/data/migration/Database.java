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
 */
@SpringBootApplication
public class Database {

    static {
        // File `import.sql` is loaded, but for readability confirm commands are multi-line
        System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor"
                , "org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");
    }

    public static void main(String[] args) throws Exception {
        Bootstrap.main(args);
    }
}
