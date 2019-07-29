package com.hazelcast.hibernate.util;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {

    public static void createDatabase(String username, String password, String databaseName){

        Connection connection;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/?user="+username+"&password="+password);
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE DATABASE "+databaseName);

        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static void dropDatabase(String username, String password, String databaseName){

        Connection connection;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/?user="+username+"&password="+password);
            Statement st = connection.createStatement();
            st.executeUpdate("DROP DATABASE "+databaseName);

        } catch (SQLException e){
            e.printStackTrace();
        }

    }

}
