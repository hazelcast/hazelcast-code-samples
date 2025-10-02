package com.hazelcast.samples.testing;

/**
 * Exception type for service-level failures.
 *
 * <p>Used to wrap lower-level exceptions and provide a
 * consistent contract at the service boundary.
 */
public class ServiceException extends RuntimeException {

    /**
     * Create a new exception with a message.
     *
     * @param message description of the failure
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a message and cause.
     *
     * @param message description of the failure
     * @param cause   underlying exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
