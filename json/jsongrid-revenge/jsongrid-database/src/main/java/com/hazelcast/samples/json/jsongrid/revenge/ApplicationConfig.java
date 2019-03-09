package com.hazelcast.samples.json.jsongrid.revenge;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * <p>Spring style configuration to create an instance of the
 * <a href="http://hsqldb.org/">HSQL Database</a>
 * using file based storage, for JDBC/JPA access.
 * </p>
 */
@Configuration
public class ApplicationConfig {

    /**
     * <p>Create a file based version of the
     * <a href="http://hsqldb.org/">HSQL Database</a> to use as our relational
     * database. A memory based one would do too.
     * </p>
     * <p>The database name is "{@code mydatabase}" which matches the
     * "{@code .gitignore}" file.
     * </p>
     *
     * @return A running database server.
     * @throws AclFormatException It shouldn't!
     * @throws IOException It shouldn't
     */
    @Bean
    public Server server() throws IOException, AclFormatException {
        Server server = new Server();

        String myDataBase = "mydatabase";
        server.setDatabaseName(0, myDataBase);
        server.setDatabasePath(0, "file:" + myDataBase + "db");

        Properties properties = new Properties();
        properties.put("maxdatabases", "1");
        HsqlProperties hsqlProperties  = new HsqlProperties(properties);
        server.setProperties(hsqlProperties);

        server.start();

        return server;
    }

    /**
     * <p>Create an explicit database to stop Spring Boot from trying
     * to soon. Inject the {@link org.hsqldb.Server Server} {@code @Bean}
     * so this is created first.
     * </p>
     *
     * @param server {@code @Bean} created above
     * @param password From {@code application.yml}
     * @param url From {@code application.yml}
     * @param username From {@code application.yml}
     * @return A data-source onto the database above
     */
    @Bean
    public DataSource dataSource(Server server
    	    ,@Value("${spring.datasource.password}") String password
    	    ,@Value("${spring.datasource.url}") String url
    	    ,@Value("${spring.datasource.username}") String username
    		) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
    
}
