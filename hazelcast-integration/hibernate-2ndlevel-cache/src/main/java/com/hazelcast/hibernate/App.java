package com.hazelcast.hibernate;

import com.hazelcast.hibernate.entity.TestObject;
import com.hazelcast.hibernate.util.DatabaseUtil;
import com.hazelcast.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;

import java.util.Scanner;

public class App {

    public static String DB_USERNAME;
    public static String DB_PASSWORD;
    public static String DB_NAME;


    public static void main(String[] args) {

        int ENTITY_COUNT = 10;

        configureDatabase(args);
        DatabaseUtil.createDatabase(DB_USERNAME, DB_PASSWORD, DB_NAME);


        Session session1, session2, session3;
        Transaction transaction1, transaction2, transaction3;

        // Session1 insert operations
        session1 = HibernateUtil.getSessionFactory().openSession();
        transaction1 = session1.beginTransaction();

        for (int i = 0; i < ENTITY_COUNT; i++) {
            session1.save(new TestObject(i, i));

            if(i % 20 == 0){
                session1.flush();
                session1.clear();
            }
        }

        transaction1.commit();
        session1.close();
        printStats();


        // Session2 get operations
        // No sql queries must be seen and the objects must be fetched from secondary cache
        session2 = HibernateUtil.getSessionFactory().openSession();
        transaction2 = session2.beginTransaction();
        for (int i = 0; i < ENTITY_COUNT; i++) {
            TestObject tmp = session2.get(TestObject.class, i);
            System.out.println(tmp);
            if(i % 20 == 0){
                session2.flush();
                session2.clear();
            }
        }

        transaction2.commit();
        session2.close();
        printStats();

        HibernateUtil.getSessionFactory().getCache().evictAll();

        // Session3 get operations when cache is evicted
        // Sql queries must be seen and the objects must be fetched from database
        session3 = HibernateUtil.getSessionFactory().openSession();
        transaction3 = session3.beginTransaction();
        for (int i = 0; i < ENTITY_COUNT; i++) {
            session3.get(TestObject.class, i);
            if(i % 20 == 0){
                session3.flush();
                session3.clear();
            }
        }


        transaction3.commit();
        session3.close();
        printStats();

        HibernateUtil.closeFactory();
        DatabaseUtil.dropDatabase(DB_USERNAME, DB_PASSWORD, DB_NAME);
        System.exit(0);

    }

    static void configureDatabase(String[] args){
        boolean use_db_config = args.length == 3;

        DB_USERNAME = use_db_config ? args[0] : "root";
        DB_PASSWORD = use_db_config ? args[1] : "root";
        DB_NAME = use_db_config ? args[2] : "hazelcast_demo_db";
    }

    static void printStats(){
        long hit,miss,put;
        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        hit = stats.getSecondLevelCacheHitCount();
        miss = stats.getSecondLevelCacheMissCount();
        put = stats.getSecondLevelCachePutCount();
        System.out.printf("[STATS] hit: %d\t miss: %d\t put: %d\n", hit,miss,put);
    }
}
