package com.hazelcast.examples.declarative;

import javax.cache.configuration.Factory;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;

/**
 * TODO: add a proper JavaDoc
 */
public class ExpiryPolicyFactory implements Factory<ExpiryPolicy> {

    @Override
    public ExpiryPolicy create() {
        return new EternalExpiryPolicy();
    }
}
