package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import com.hazelcast.hibernate.springhibernate2lc.persistence.BookRepository;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringHibernate2lcApplicationTests {

	private static Logger logger = LoggerFactory.getLogger(SpringHibernate2lcApplicationTests.class);

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EntityManager entityManager;

	@Test
	void testCaching() {
		//see Runner class for the data
		printCachesFromHazelcast();

		Session session = (Session) entityManager.getDelegate();

		printCachesFromCacheManager();

		Statistics statistics = session.getSessionFactory().getStatistics();
		statistics.logSummary();

		assertThat(statistics.getQueryCacheHitCount()).isEqualTo(1);
		assertThat(statistics.getQueryCachePutCount()).isEqualTo(1);
		assertThat(statistics.getQueryCacheMissCount()).isEqualTo(1);
		assertThat(statistics.getSecondLevelCacheHitCount()).isEqualTo(5);
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

	private void findAllBooks() {
		Iterable<Book> people = transactionTemplate.execute(status -> bookRepository.findAll());
		logger.info("Loaded all people: " + people);
	}

	private Optional<Book> getBookById(long id) {
		Optional<Book> book = transactionTemplate.execute(status -> bookRepository.findById(id));
		book.ifPresent(p -> logger.info("Loaded book: {}", p));
		return book;
	}

}
