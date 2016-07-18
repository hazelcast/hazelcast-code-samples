package com.hazelcast.spring.transaction;


import com.hazelcast.transaction.TransactionalTaskContext;
import org.springframework.transaction.annotation.Transactional;

public class ServiceWithTransactionalMethod {


    private TransactionalTaskContext transactionalTaskContext;

    @Transactional
    public void transactionalPut(String key, String value) {
        transactionalTaskContext.getMap("testMap").put(key, value);
    }

    @Transactional
    public void transactionalPutWithException(String key, String value) {
        transactionalTaskContext.getMap("testMap").put(key, value);
        throw new RuntimeException();
    }

    public void setTransactionalTaskContext(TransactionalTaskContext transactionalTaskContext) {
        this.transactionalTaskContext = transactionalTaskContext;
    }
}
