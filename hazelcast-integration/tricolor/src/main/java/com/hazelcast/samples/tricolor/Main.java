package com.hazelcast.samples.tricolor;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CountDownLatch;

/**
 * Demonstrate the use of separate class loaders, so that
 * three Hazelcast instances can run in the one JVM with
 * different class hierarchies.
 * <p>
 * There are few situations in Production where you would
 * run more than one instance in a JVM. You may as well run
 * one to soak up all power available.
 * <p>
 * Here we do this so they can have different loggers, using
 * different colours. This might be a thing you do to make
 * testing client-server operations easier for humans to interpret
 * the results.
 * <p>
 * Another use might be to test different versions, you could
 * have a server version 3.8 and a client version 3.6.
 * <p>
 * All of this of course of <u>much</u> easier with separate
 * JVMs. The purpose of this example is to use one JVM, as that's
 * more suitable for controlled tests. With multiple JVMs the
 * sequence that things happen is never quite deterministic.
 * <hr>
 * Feel free to amend this example to suit your needs. Take
 * great care to understand that output using the likes of
 * {@code System.out.println} is clumsy but safe. Any time
 * you use logging you have inadvertently loaded logger classes
 * and configuration.
 */
public class Main {

    static {
        // tell Hazelcast to use SLF4J logging
        System.setProperty("hazelcast.logging.type", "slf4j");
    }

    /**
     * Create an instance so as to avoid static methods.
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.go();

        System.exit(0);
    }

    /**
     * Create three threads, start them, and wait for them to finish.
     * The first two are Hazelcast servers, and the latter is a Hazelcast
     * client.
     * <p>
     * Although they all run in this JVM, what is different is they run
     * in different class loaders so do not share classes. This allows them
     * to have different logging implementations.
     *
     * @throws Exception Should not occur!
     */
    private void go() throws Exception {
        System.out.println("-=-=-=-=-=-=-=-=-=-=- START -=-=-=-=-=-=-=-=-=-=-");

        Class<?> myServerClass1 = myLoadClass("com.hazelcast.samples.tricolor.MyServer");
        Class<?> myServerClass2 = myLoadClass("com.hazelcast.samples.tricolor.MyServer");
        Class<?> myClientClass = myLoadClass("com.hazelcast.samples.tricolor.MyClient");

        // ensure these are different
        if (myServerClass1 == myServerClass2) {
            throw new Exception("Separated class loading has failed");
        }

        // create two servers and a client as separate threads
        Thread[] threads = new Thread[3];
        CountDownLatch countDownLatch = new CountDownLatch(threads.length - 1);

        Runnable server1 = new MyRunnable(countDownLatch, myServerClass1, "blue.xml");
        threads[0] = new Thread(server1);

        Runnable server2 = new MyRunnable(countDownLatch, myServerClass2, "green.xml");
        threads[1] = new Thread(server2);

        Runnable client = new MyRunnable(countDownLatch, myClientClass, "red.xml");
        threads[2] = new Thread(client);

        // start the threads
        for (Thread thread : threads) {
            thread.start();
        }

        // wait for them to finish
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("-=-=-=-=-=-=-=-=-=-=-  END  -=-=-=-=-=-=-=-=-=-=-");
    }

    /**
     * Create a fresh class loader and load a class by name from the classpath.
     * <p>
     * If we call this twice with the same argument, such as
     * "{@code com.hazelcast.samples.tricolor.MyServer}" it will return
     * two objects of different classes. The classes will have the
     * same definitions since they are loaded from the same classpath
     * but won't be the same object.
     *
     * @param name Class name, "com.something.something.Something"
     * @return Class in a different class loader to the caller
     */
    private Class<?> myLoadClass(String name) throws Exception {
        try {
            // the class loader for Main()
            ClassLoader currentClassLoader = getClass().getClassLoader();

            if (currentClassLoader instanceof URLClassLoader) {
                // create a classloader from the parent of this one
                URL[] urls = ((URLClassLoader) currentClassLoader).getURLs();
                URLClassLoader childClassLoader = new URLClassLoader(urls, currentClassLoader.getParent());

                // use the new classloader to load the class
                return Class.forName(name, true, childClassLoader);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static class MyRunnable implements Runnable {

        private final CountDownLatch countDownLatch;
        private final Class<?> nodeClass;
        private final String xml;

        MyRunnable(CountDownLatch countDownLatch, Class<?> nodeClass, String xml) {
            this.countDownLatch = countDownLatch;
            this.nodeClass = nodeClass;
            this.xml = xml;
        }

        @Override
        public void run() {
            try {
                Object hazelcastInstance = nodeClass.getDeclaredConstructor(String.class, CountDownLatch.class)
                        .newInstance(xml, countDownLatch);
                nodeClass.getMethod("run")
                        .invoke(hazelcastInstance);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
