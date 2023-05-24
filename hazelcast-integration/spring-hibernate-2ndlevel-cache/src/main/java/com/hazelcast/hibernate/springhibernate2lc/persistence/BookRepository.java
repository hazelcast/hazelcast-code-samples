package com.hazelcast.hibernate.springhibernate2lc.persistence;

import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
    @Override
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    Iterable<Book> findAll();
}
