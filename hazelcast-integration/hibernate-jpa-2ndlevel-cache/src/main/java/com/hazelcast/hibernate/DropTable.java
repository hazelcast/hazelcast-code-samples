package com.hazelcast.hibernate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DropTable {

    public static void main(String[] args) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:derby:hibernateDB;create=true", new Properties());
            Statement st = conn.createStatement();
            st.executeUpdate("DROP TABLE employee");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
