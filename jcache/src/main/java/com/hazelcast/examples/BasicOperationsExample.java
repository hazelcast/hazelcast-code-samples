package com.hazelcast.examples;


public class BasicOperationsExample  extends AbstractApp {


    public BasicOperationsExample() {
    }

    public static void main(String[] args) throws InterruptedException {
        BasicOperationsExample app = new BasicOperationsExample();

        app.initCacheManager();
        app.initCache("theCache");

        app.populateCache();
        app.printContent();

        Thread.sleep(10 * 1000);

        app.printContent();

        app.shutdown();


    }
}
