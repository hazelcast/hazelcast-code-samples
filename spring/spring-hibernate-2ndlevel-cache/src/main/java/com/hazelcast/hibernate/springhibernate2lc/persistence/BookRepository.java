package com.hazelcast.hibernate.springhibernate2lc.persistence;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
    @Override
    //This query hint enables second level caching of query results
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    Iterable<Book> findAll();
}
