package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import com.hazelcast.hibernate.springhibernate2lc.persistence.BookRepository;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
class Runner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    private final BookRepository repository;

    private final HazelcastInstance hazelcastInstance;

    private final TransactionTemplate transactionTemplate;

    private final CacheManager cacheManager;

    private final EntityManager entityManager;

    public Runner(BookRepository repository, HazelcastInstance hazelcastInstance,
                  TransactionTemplate transactionTemplate, CacheManager cacheManager, EntityManager entityManager) {
        this.repository = repository;
        this.hazelcastInstance = hazelcastInstance;
        this.transactionTemplate = transactionTemplate;
        this.cacheManager = cacheManager;
        this.entityManager = entityManager;
    }

    @Override
    public void run(String... args) throws Exception {
        Book book1 = new Book();
        book1.setName("Alice in Wonderland");
        Book book2 = new Book();
        book2.setName("");

        List<Book> people = List.of(book1, book2);
        logger.info("Inserting people {}", people);
        transactionTemplate.executeWithoutResult(status -> repository.saveAll(people));

        getBookById(1L); // cache put
        getBookById(2L); // cache put
        getBookById(2L); // cache hit


        getBookById(3L); // cache miss


        findAllBooks(); // 2 cache hits, query cache miss, query cache put
        findAllBooks(); // 2 cache hits, query cache hit

        printCachesFromHazelcast();

        Session session = (Session) entityManager.getDelegate();

        printCachesFromCacheManager();

        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.logSummary();
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
        Iterable<Book> people = transactionTemplate.execute(status -> repository.findAll());
        logger.info("Loaded all people: " + people);
    }

    private Optional<Book> getBookById(long id) {
        Optional<Book> book = transactionTemplate.execute(status -> repository.findById(id));
        book.ifPresent(p -> logger.info("Loaded book: {}", p));
        return book;
    }
}
