package com.hazelcast.samples.tricolor;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CountDownLatch;

/**
 * <P>Demonstrate the use of separate class loaders, so that
 * three Hazelcast instances can run in the one JVM with
 * different class hierarchies.
 * </P>
 * <P>There are few situations in Production where you would
 * run more than one instance in a JVM. You may as well run
 * one to soak up all power available.
 * </P>
 * <P>Here we do this so they can have different loggers, using
 * different colours. This might be a thing you do to make
 * testing client-server operations easiers for humans to interpret
 * the results.
 * </P>
 * <P>Another use might be to test different versions, you could
 * have a server version 3.8 and a client version 3.6.
 * </P>
 * <P>All of this of course of <U>much</U> easier with separate
 * JVMs. The purpose of this example is to use one JVM, as that's
 * more suitable for controlled tests. With multiple JVMs the
 * sequence that things happen is never quite deterministic.
 * </P>
 * <HR/>
 * <P>Feel free to amend this example to suit your needs. Take
 * great care to understand that output using the likes of
 * {@code System.out.println} is clumsy but safe. Any time
 * you use logging you have inadvertently loaded logger classes
 * and configuration.
 * </P>
 */
public class Main {

	/**
	 * <P>Tell Hazelcast to use SLF4J logging
	 * </P>
	 */
	static {
		System.setProperty("hazelcast.logging.type", "slf4j");
	}
		
	/**
	 * <P>Create an instance so as to avoid static methods.
	 * </P>
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.go();
		
		System.exit(0);
	}

	/**
	 * <P>Create three threads, start them, and wait for them to finish.
	 * The first two are Hazelcast servers, and the latter is a Hazelcast
	 * client.
	 * </P>
	 * <P>Although they all run in this JVM, what is different is they run
	 * in different class loaders so do not share classes. This allows them
	 * to have different logging implementations.
	 * </P>
	 * 
	 * @throws Exception Should not occur!
	 */
	private void go() throws Exception {
		System.out.println("-=-=-=-=-=-=-=-=-=-=- START -=-=-=-=-=-=-=-=-=-=-");
		
		Class<?> myServerClass1 = this.myLoadClass("com.hazelcast.samples.tricolor.MyServer");
		Class<?> myServerClass2 = this.myLoadClass("com.hazelcast.samples.tricolor.MyServer");
		Class<?> myClientClass = this.myLoadClass("com.hazelcast.samples.tricolor.MyClient");
		
		/* Ensure these are different
		 */
		if (myServerClass1 == myServerClass2) {
			throw new Exception("Separated class loading has failed");
		}

		/* Create two servers and a client as separate threads
		 */
		Thread[] threads = new Thread[3];
		CountDownLatch countDownLatch = new CountDownLatch(threads.length - 1);

		Runnable server1 = () -> {
			try {
				Object myServer1 = 
						myServerClass1.getDeclaredConstructor(String.class, CountDownLatch.class)
						.newInstance("blue.xml", countDownLatch);
				myServerClass1.getMethod("run").invoke(myServer1);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		};
		threads[0] = new Thread(server1);
		
		Runnable server2 = () -> {
			try {
				Object myServer2 = 
						myServerClass2.getDeclaredConstructor(String.class, CountDownLatch.class)
						.newInstance("green.xml", countDownLatch);
				myServerClass2.getMethod("run").invoke(myServer2);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		};
		threads[1] = new Thread(server2);
		
		Runnable client = () -> {
			try {
				Object myClient = myClientClass.getDeclaredConstructor(String.class, CountDownLatch.class)
						.newInstance("red.xml", countDownLatch);
				myClientClass.getMethod("run").invoke(myClient);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		};
		threads[2] = new Thread(client);

		// Start the threads
		for (Thread thread : threads) {
			thread.start();
		}
		
		// Wait for them to finish
		for (Thread thread : threads) {
			thread.join();
		}
		
		System.out.println("-=-=-=-=-=-=-=-=-=-=-  END  -=-=-=-=-=-=-=-=-=-=-");
	}

	/**
	 * <P>Create a fresh class loader, and load a class by name from the classpath.
	 * </P>
	 * <P>If we call this twice with the same argument, such as
	 * "{@code com.hazelcast.samples.tricolor.MyServer}" it will return
	 * two objects of different classes. The classes will have the
	 * same definitions since they are loaded from the same classpath
	 * but won't be the same object.
	 * </P>
	 * 
	 * @param name Class name, "com.something.something.Something"
	 * @return Class in a different class loader to the caller
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 */
	public Class<?> myLoadClass(String name) throws ClassNotFoundException, MalformedURLException {
		try {
			// The class loader for Main()
			ClassLoader currentClassLoader = this.getClass().getClassLoader();
			
			if (currentClassLoader instanceof URLClassLoader) {
				// Create a classloader from the parent of this one
				URL[] urls = ((URLClassLoader) currentClassLoader).getURLs();
				URLClassLoader childClassLoader = new URLClassLoader(urls, currentClassLoader.getParent());
				
				// Use the new classloader to load the class
				return Class.forName(name, true, childClassLoader);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
