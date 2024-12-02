package com.hazelcast.samples.connection.pool;

import com.hazelcast.shaded.com.zaxxer.hikari.HikariConfig;
import com.hazelcast.shaded.com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariDataSourcePool {
    private static HikariDataSource hikariDataSource = null;
    private static final HikariDataSourcePool hikariDataSourcePool = null;

    private HikariDataSourcePool() {
        if (null != hikariDataSource) {
            System.out.println("Hikari data source already created. existing connection can be used.");
        } else {
            createHikariDataSource();
        }
    }

    private void createHikariDataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setPoolName("Demo-POOL");
        hikariConfig.setDriverClassName("org.h2.Driver");

        hikariDataSource = new HikariDataSource(hikariConfig);

        System.out.println("Datasource Created..");
    }

    public static synchronized Connection getConnection() {
        try {
            if (null != hikariDataSource) {
                System.err.println("\nGetting....! SQL Connection from HIKARI POOL.\n");
                return hikariDataSource.getConnection();
            } else {
                throw new RuntimeException("Ops! Hikari datasource not available.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Exception while creating database connection." + exception);
        }
    }
}
