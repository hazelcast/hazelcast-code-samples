package com.hazelcast.springboot.caching;

import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;

// tag::BookService[]
@Service
public class BookService {
    @CacheResult(cacheName = "books")
    public String getBookNameByIsbn(String isbn) {
        return findBookInSlowSource(isbn);
    }

    private String findBookInSlowSource(String isbn) {
        // some long processing
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return "Sample Book Name";
    }
}
// end::BookService[]
