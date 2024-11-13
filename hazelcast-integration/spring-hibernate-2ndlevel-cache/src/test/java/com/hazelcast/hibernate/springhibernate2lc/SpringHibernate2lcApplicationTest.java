package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.springhibernate2lc.persistence.BookRepository;
import com.hazelcast.instance.BuildInfoProvider;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.Version;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Ignore("Need to release plugins for 4.0 first")
class SpringHibernate2lcApplicationTest {

	private static Logger logger = LoggerFactory.getLogger(SpringHibernate2lcApplicationTest.class);

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EntityManager entityManager;

	@Test
	void testCaching() {
		logger.warn("Hazelcast: " + BuildInfoProvider.getBuildInfo().getVersion());
		logger.warn("Hibernate: " + Version.getVersionString());
		logger.warn("Spring: " + SpringBootVersion.getVersion());

		//see Runner class for the data
		printCachesFromHazelcast();

		Session session = (Session) entityManager.getDelegate();

		printCachesFromCacheManager();

		Statistics statistics = session.getSessionFactory().getStatistics();
		statistics.logSummary();

		assertThat(statistics.getQueryCacheHitCount()).isEqualTo(1);
		assertThat(statistics.getQueryCachePutCount()).isEqualTo(1);
		assertThat(statistics.getQueryCacheMissCount()).isEqualTo(1);
		assertThat(statistics.getSecondLevelCacheHitCount()).isEqualTo(3);
		assertThat(statistics.getSecondLevelCacheMissCount()).isEqualTo(1);
		assertThat(statistics.getSecondLevelCachePutCount()).isEqualTo(2);
	}

	private void printCachesFromCacheManager() {
		Collection<String> cacheNames = cacheManager.getCacheNames();
		logger.info("Caches from cacheManager:");
		for (String cacheName : cacheNames) {
			logger.info(" - " + cacheName);
		}
	}

	private void printCachesFromHazelcast() {
		logger.info("Caches from hazelcast:");
		for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
			logger.info(" - " + distributedObject.getName());
		}
	}

}
