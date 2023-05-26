package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import com.hazelcast.hibernate.springhibernate2lc.persistence.BookRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
class Runner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

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
        LOGGER.info("Inserting people {}", people);

        // 2 cache puts
        repository.saveAll(people);

        //cache put, cache hit:
        getBookById(1L);

        // cache put, cache hit
        getBookById(2L);

        // cache hit
        getBookById(2L);

        // cache miss
        getBookById(3L);

        // query cache miss, query cache put (WITH entities instead of ids only)
        findAllBooks();

        // query cache hit
        findAllBooks();

        printCachesFromHazelcast();

        Session session = (Session) entityManager.getDelegate();

        printCachesFromCacheManager();

        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.logSummary();
    }

    private void printCachesFromCacheManager() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        LOGGER.info("Caches from cacheManager:");
        for (String cacheName : cacheNames) {
            LOGGER.info(" - " + cacheName);
        }
    }

    private void printCachesFromHazelcast() {
        LOGGER.info("Caches from hazelcast:");
        for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
            LOGGER.info(" - " + distributedObject.getName());
        }
    }

    private void findAllBooks() {
        Iterable<Book> people = repository.findAll();
        LOGGER.info("Loaded all people: " + people);
    }

    private Optional<Book> getBookById(long id) {
        Optional<Book> book = repository.findById(id);
        book.ifPresent(p -> LOGGER.info("Loaded book: {}", p));
        return book;
    }
}
