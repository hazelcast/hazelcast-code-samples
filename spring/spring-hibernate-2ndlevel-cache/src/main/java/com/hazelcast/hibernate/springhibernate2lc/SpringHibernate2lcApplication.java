package com.hazelcast.hibernate.springhibernate2lc;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.cache.CacheManager;

// Periodically show the contents of the Hibernate cache.
@EnableScheduling
@SpringBootApplication
public class SpringHibernate2lcApplication {

    @Bean(destroyMethod = "")
    CacheManager hibernateCacheManager(EntityManagerFactory entityManagerFactory) {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        JCacheRegionFactory regionFactory = (JCacheRegionFactory) sessionFactory.getCache().getRegionFactory();
        return regionFactory.getCacheManager();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringHibernate2lcApplication.class, args);
    }
}
