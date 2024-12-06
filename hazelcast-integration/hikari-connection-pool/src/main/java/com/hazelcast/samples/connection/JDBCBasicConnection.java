package com.hazelcast.samples.connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCBasicConnection {
    public static void getConnection() {
        try {
            Class.forName("org.h2.Driver");
            DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
