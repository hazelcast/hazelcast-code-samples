package com.hazelcast.springconfiguration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IAtomicReference;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.Serializable;
import java.util.Random;

public class HazelcastDataTypes {

    private static final Random RANDOM = new Random();

    private static ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
    private static TestBean testBean = (TestBean) context.getBean("springTestBean");

    public static void main(String[] args) {
        System.out.println(testBean.getResult());
        System.out.println();

        executeMap();
        executeMultiMap();
        executeReplicatedMap();
        executeQueue();
        executeTopic();
        executeSet();
        executeList();
        executeExecutorService();
        executeIdGenerator();
        executeAtomicLong();
        executeAtomicReference();
        executeCountDownLatch();
        executeSemaphore();
        executeLock();

        Hazelcast.shutdownAll();
        HazelcastClient.shutdownAll();
    }

    private static void executeMap() {
        System.out.println("### Map Execution Started... ###");
        int key = RANDOM.nextInt(100);
        int value = RANDOM.nextInt(100);
        IMap<Integer, Integer> map = (IMap<Integer, Integer>) context.getBean("map");
        map.put(key, value);
        System.out.println("A random pair is added to map.");
        System.out.println("Added value: " + map.get(key) + "\n");
    }

    private static void executeMultiMap() {
        System.out.println("### MultiMap Execution Started... ###");
        int key = RANDOM.nextInt(100);
        int value = RANDOM.nextInt(100);
        MultiMap<Integer, Integer> multimap = (MultiMap<Integer, Integer>) context.getBean("multiMap");
        multimap.put(key, value);
        System.out.println("A random pair is added to multiMap.");
        System.out.println("Added value: " + multimap.get(key) + "\n");
    }

    private static void executeReplicatedMap() {
        System.out.println("### ReplicatedMap Execution Started... ###");
        int key = RANDOM.nextInt(100);
        int value = RANDOM.nextInt(100);
        ReplicatedMap<Integer, Integer> replicatedMap = (ReplicatedMap<Integer, Integer>) context.getBean("replicatedMap");
        replicatedMap.put(key, value);
        System.out.println("A random pair is added to replicatedMap.");
        System.out.println("Added value: " + replicatedMap.get(key) + "\n");
    }

    private static void executeQueue() {
        System.out.println("### Queue Execution Started... ###");
        int key = RANDOM.nextInt(100);
        IQueue<Integer> queue = (IQueue<Integer>) context.getBean("queue");
        queue.add(key);
        System.out.println("A random integer is added to queue.");
        System.out.println("Added element: " + queue.poll() + "\n");
    }

    private static void executeTopic() {
        System.out.println("### Topic Execution Started... ###");
        ITopic<String> topic = (ITopic<String>) context.getBean("topic");
        topic.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                System.out.println("Topic Received Message: " + message + "\n");
            }
        });
        topic.publish("object");
    }

    private static void executeSet() {
        System.out.println("### Set Execution Started... ###");
        int key = RANDOM.nextInt(100);
        ISet<Integer> set = (ISet<Integer>) context.getBean("set");
        set.add(key);
        System.out.println("A random integer is added to set.");
        System.out.println("Added element: " + set.iterator().next() + "\n");
    }

    private static void executeList() {
        System.out.println("### List Execution Started... ###");
        int key = RANDOM.nextInt(100);
        IList<Integer> list = (IList<Integer>) context.getBean("list");
        list.add(key);
        System.out.println("A random integer is added to list.");
        System.out.println("Added element: " + list.iterator().next() + "\n");
    }

    private static void executeExecutorService() {
        System.out.println("### ExecutorService Execution Started... ###");
        IExecutorService executorService = (IExecutorService) context.getBean("executorService");
        executorService.execute(new EchoTask("hello"));
        executorService.shutdown();
    }

    private static void executeIdGenerator() {
        System.out.println("### IdGenerator Execution Started... ###");
        IdGenerator idgenerator = (IdGenerator) context.getBean("idGenerator");
        idgenerator.init(100L);
        System.out.println("IdGenerator is initialized with 100.");
        System.out.println("NewId: " + idgenerator.newId() + "\n");
    }

    private static void executeAtomicLong() {
        System.out.println("### AtomicLong Execution Started... ###");
        IAtomicLong atomicLong = (IAtomicLong) context.getBean("atomicLong");
        atomicLong.set(100L);
        System.out.println("AtomicLong is set to 100.");
        System.out.println("AtomicLong: " + atomicLong.get() + "\n");
    }

    private static void executeAtomicReference() {
        System.out.println("### AtomicReference Execution Started... ###");
        IAtomicReference<String> atomicReference = (IAtomicReference<String>) context.getBean("atomicReference");
        atomicReference.set("Executing AtomicReference");
        System.out.println(atomicReference.get() + "\n");
    }

    private static void executeCountDownLatch() {
        System.out.println("### CountDownLatch Execution Started... ###");
        ICountDownLatch countDownLatch = (ICountDownLatch) context.getBean("countDownLatch");
        countDownLatch.trySetCount(10);
        System.out.println("Count is set to 10.");
        countDownLatch.countDown();
        System.out.println("countDown() call...");
        System.out.println("CountDownLatch Count :" + countDownLatch.getCount() + "\n");
    }

    private static void executeSemaphore() {
        System.out.println("### Semaphore Execution Started... ###");
        ISemaphore semaphore = (ISemaphore) context.getBean("semaphore");
        semaphore.init(5);
        System.out.println("Semaphore initialized with 5.");
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Semaphore acquired once.");
        System.out.println("Available semaphore permits: " + semaphore.availablePermits());
        semaphore.release();
        System.out.println("Semaphore released.");
        System.out.println("Available semaphore permits: " + semaphore.availablePermits() + "\n");
    }

    private static void executeLock() {
        System.out.println("### Lock Execution Started... ###");
        ILock lock = (ILock) context.getBean("lock");
        lock.lock();
        System.out.println("lock() call...");
        System.out.println("is locked by current thread? :" + lock.isLockedByCurrentThread());
        lock.unlock();
        System.out.println("unlock() call...");
        System.out.println("is locked? :" + lock.isLocked());
    }

    private static class EchoTask implements Runnable, Serializable {

        private final String msg;

        EchoTask(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            System.out.println("echo:" + msg);
        }
    }
}
