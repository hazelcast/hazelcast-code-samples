package com.hazelcast.hibernate.util;

import com.hazelcast.hibernate.App;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.hazelcast.hibernate.entity.TestObject;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    public static SessionFactory getSessionFactory() {
        String DB_NAME = App.DB_NAME;
        String DB_USERNAME = App.DB_USERNAME;
        String DB_PASSWORD = App.DB_PASSWORD;
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration()
                        .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                        .setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/"+DB_NAME+"?useSSL=false&allowPublicKeyRetrieval=true")
                        .setProperty("hibernate.connection.username", DB_USERNAME)
                        .setProperty("hibernate.connection.password", DB_PASSWORD)
                        .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
                        .setProperty("hibernate.show_sql", "true")
                        .setProperty("hibernate.current_session_context_class", "thread")
                        .setProperty("hibernate.hbm2ddl.auto", "create")
                        .setProperty("hibernate.cache.use_query_cache", "true")
                        .setProperty("hibernate.cache.use_second_level_cache", "true")
                        .setProperty("hibernate.cache.region.factory_class","com.hazelcast.hibernate.HazelcastCacheRegionFactory")
                        .setProperty("hibernate.generate_statistics","true")
                        .addAnnotatedClass(TestObject.class);
                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static void closeFactory(){
        sessionFactory.close();

    }
}
