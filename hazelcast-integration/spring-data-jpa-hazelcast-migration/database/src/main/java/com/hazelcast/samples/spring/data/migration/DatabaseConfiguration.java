package com.hazelcast.samples.spring.data.migration;

import org.hsqldb.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/**
 * Create a running database server, and a database inside it.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration {

    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.url}")
    private String url;

    /**
     * The server can support multiple, but only create one database, the first of ten possibles.
     *
     * @return A running database server.
     */
    @Bean
    public Server server() {
        String myDataBase = "mydatabase";

        Server server = new Server();

        server.setDatabaseName(0, myDataBase);
        server.setDatabasePath(0, "file:" + myDataBase + "db");

        server.start();

        return server;
    }

    /**
     * Create an explicit database to stop Spring Boot from trying.
     *
     * Force creation after the server, to avoid dependency errors.
     *
     * @return A datasource onto the database above
     */
    @Bean
    @DependsOn("server")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(this.url)
                .username(this.username)
                .password(this.password)
                .build();
    }
}
