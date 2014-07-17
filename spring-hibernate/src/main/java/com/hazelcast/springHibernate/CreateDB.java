package com.hazelcast.springHibernate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 08.07.2014.
 */
public class CreateDB {
    public static void main( String args[] ){
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        }
        catch( Exception e ){
            System.out.println(e);
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:derby:springHibernateDB;create=true", new Properties());
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE TABLE  CUSTOMER(" +
                                "ID VARCHAR(255) PRIMARY KEY NOT NULL," +
                                "CITY VARCHAR(255) NOT NULL," +
                                "COUNTRY VARCHAR(255) NOT NULL," +
                                "STREET_NAME VARCHAR(255) NOT NULL," +
                                "NAME VARCHAR(255) NOT NULL," +
                                "SURNAME VARCHAR(255) NOT NULL" +
                                ")");
        }
        catch( SQLException s){
            System.out.println(s);
        }
    }
}
