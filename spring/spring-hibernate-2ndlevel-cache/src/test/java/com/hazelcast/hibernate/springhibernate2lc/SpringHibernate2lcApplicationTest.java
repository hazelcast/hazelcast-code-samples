package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.instance.BuildInfoProvider;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.Version;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;

import javax.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringHibernate2lcApplicationTest {

	private static Logger logger = LoggerFactory.getLogger(SpringHibernate2lcApplicationTest.class);

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EntityManager entityManager;

	@Test
	void testCaching() {
		logger.warn("Hazelcast: " + BuildInfoProvider.getBuildInfo().getVersion());
		logger.warn("Hibernate: " + Version.getVersionString());
		logger.warn("Spring: " + SpringBootVersion.getVersion());

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
		logger.info("Caches from cacheManager:");
		for (String cacheName : cacheManager.getCacheNames()) {
			logger.info(" - " + cacheName);
		}
	}
}
