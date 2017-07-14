package com.hazelcast.hibernate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Esref Ozturk <esrefozturk93@gmail.com>
 */
public class CreateTable {

    public static void main(String[] args) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:derby:hibernateDB;create=true", new Properties());
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE TABLE employee(id INT PRIMARY KEY NOT NULL"
                    + ", first_name VARCHAR(20) default NULL"
                    + ", last_name  VARCHAR(20) default NULL"
                    + ", salary     INT         default NULL)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
