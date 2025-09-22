package com.hazelcast.samples.testing;

/**
 * Service's own exception, raised to wrap lower level exception
 */
public class ServiceException
        extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
