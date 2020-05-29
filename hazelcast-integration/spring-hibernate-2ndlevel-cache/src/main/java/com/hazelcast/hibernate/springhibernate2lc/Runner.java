package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.springhibernate2lc.persistence.BookRepository;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class Runner implements CommandLineRunner {

    private final HazelcastInstance instance;

    private final BookRepository repository;

    public Runner(HazelcastInstance instance, BookRepository repository) {
        this.instance = instance;
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        repository.save(new Book(42, "foo"));
        repository.findAll().forEach(System.out::println);
    }
}
