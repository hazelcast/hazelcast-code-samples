package com.hazelcast.springHibernate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
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
            st.executeUpdate("CREATE TABLE  stock(" +
                    "  STOCK_ID INT PRIMARY KEY NOT NULL," +
                    "  STOCK_CODE VARCHAR(10) NOT NULL," +
                    "  STOCK_NAME VARCHAR(20) NOT NULL)");
            st.executeUpdate( "insert into stock values( 1 , '7668' , 'asd' )" );
        }
        catch( SQLException s){
            System.out.println(s);
        }
    }
}