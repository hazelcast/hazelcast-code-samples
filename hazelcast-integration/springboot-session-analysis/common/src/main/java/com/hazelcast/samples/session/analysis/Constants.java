package com.hazelcast.samples.session.analysis;

/**
 * <p>Utility constants for clients and servers to share.
 * </p>
 */
public class Constants {

    // Map names
    public static final String IMAP_NAME_JSESSIONID = "jsessionid";
    public static final String[] IMAP_NAMES = { IMAP_NAME_JSESSIONID };

    // Cluster group
    public static final String MY_GROUP_NAME = "sessions";
    public static final String MY_GROUP_PASSWORD = MY_GROUP_NAME;

}
