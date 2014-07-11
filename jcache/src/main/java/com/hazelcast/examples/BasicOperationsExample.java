package com.hazelcast.examples;

/**
 * Basic example
 * Configures a cache with access expiry of 10 secs.
 *
 */
public class BasicOperationsExample  extends AbstractApp {


    public BasicOperationsExample() {
    }

    public static void main(String[] args) throws InterruptedException {

        //create the app
        BasicOperationsExample app = new BasicOperationsExample();

        //first thin is we need to initialize the cache Manager
        app.initCacheManager();

        //create a cache with the provied name
        app.initCache("theCache");

        //lets populate the content
        app.populateCache();

        //so we print the content whatever we have
        app.printContent();

        //lets wait for 10 sec to expire the content
        Thread.sleep(10 * 1000);

        //and print the content again, and see everything has expired and values are null
        app.printContent();

        //lastly shutdown the cache manager
        app.shutdown();


    }
}
