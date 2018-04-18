package com.hazelcast.samples.session.analysis;

/**
 * <p>Utility constants for clients and servers to share.
 * </p>
 */
public class Constants {

    // Map names
    public static final String IMAP_NAME_JSESSIONID = "jsessionid";
    public static final String IMAP_NAME_SEQUENCE = "sequence";
    public static final String IMAP_NAME_STOCK = "stock";
    public static final String[] IMAP_NAMES = { IMAP_NAME_JSESSIONID, IMAP_NAME_SEQUENCE, IMAP_NAME_STOCK };

    // Cluster group
    public static final String MY_GROUP_NAME = "sessions";

    // HTML info
    public static final String HTML_ACTION_ADD = "add";
    public static final String HTML_ACTION_CHECKOUT = "checkout";
    public static final String HTML_ACTION_INDEX = "index";
    public static final String SESSION_ATTRIBUTE_BASKET = "basket";
    public static final String SESSION_ATTRIBUTE_BROWSER = "browser";

    // Test data, items to sell and their price
    public static final Object[][] TESTDATA = {
            { "Hat", 30 },
            { "Gloves", 20 },
            { "Scarf", 10 },
    };

}
