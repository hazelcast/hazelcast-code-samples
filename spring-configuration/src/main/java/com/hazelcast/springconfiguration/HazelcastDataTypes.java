package com.hazelcast.springconfiguration;

import com.hazelcast.core.*;
import com.hazelcast.springconfiguration.TestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import java.util.Random;

/**
 * Created by Mustafa Orkun Acar <mustafaorkunacar@gmail.com> on 07.07.2014.
 */

public class HazelcastDataTypes
{
    static Random rand = new Random();
    static ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
    static TestBean testBean = (TestBean) context.getBean("springTestBean");

    public static void main(String[] args)
    {
        System.out.println(testBean.getResult());

        ExecuteMap();
        ExecuteMultiMap();
        ExecuteQueue();
        ExecuteTopic();
        ExecuteSet();
        ExecuteList();
        ExecuteExecuterService();
        ExecuteIdGenerator();
        ExecuteAtomicLong();
        ExecuteAtomicReference();
        ExecuteCountDownLatch();
        ExecuteSemaphore();
        ExecuteLock();
    }

    public static void ExecuteMap()
    {
        System.out.println("### Map Execution Started ###");
        int k = rand.nextInt(100);
        int v = rand.nextInt(100);
        IMap map = (IMap) context.getBean("map");
        map.put(k, v);
        System.out.println(map.get(k));
    }

    public static void ExecuteMultiMap()
    {
        System.out.println("### MultiMap Execution Started ###");
        int k = rand.nextInt(100);
        int v = rand.nextInt(100);
        MultiMap<Integer, Integer> multimap = (MultiMap) context.getBean("multiMap");
        multimap.put(k, v);
        System.out.println(multimap.get(k));
    }

    public static void ExecuteQueue()
    {
        System.out.println("### Queue Execution Started ###");
        int k = rand.nextInt(100);
        IQueue queue = (IQueue) context.getBean("queue");
        queue.add(k);
        System.out.println(queue.poll());
    }

    public static void ExecuteTopic()
    {
        System.out.println("### Topic Execution Started ###");
        ITopic topic = (ITopic) context.getBean("topic");
        topic.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                System.out.println("Topic Received Message: " + message);
            }
        });
        topic.publish("object");
    }

    public static void ExecuteSet()
    {
        System.out.println("### Set Execution Started ###");
        int k = rand.nextInt(100);
        ISet set = (ISet) context.getBean("set");
        set.add(k);
        System.out.println(set.iterator().next());
    }

    public static void ExecuteList()
    {
        System.out.println("### List Execution Started ###");
        int k = rand.nextInt(100);
        IList<Integer> list = (IList<Integer>) context.getBean("list");
        list.add(k);
        System.out.println(list.iterator().next());
    }

    public static void ExecuteExecuterService()
    {
        System.out.println("### ExecuterService Execution Started ###");
        IExecutorService executorService = (IExecutorService) context.getBean("executorService");
        executorService.execute(new Runnable() {
            public void run() {
                System.out.println("ExecuterService Run");
            }
        });
        executorService.shutdown();
    }

    public static void ExecuteIdGenerator()
    {
        System.out.println("### IdGenerator Execution Started ###");
        IdGenerator idgenerator = (IdGenerator) context.getBean("idGenerator");
        idgenerator.init(100L);
        System.out.println(idgenerator.newId());
    }

    public static void ExecuteAtomicLong()
    {
        System.out.println("### AtomicLong Execution Started ###");
        IAtomicLong atomicLong = (IAtomicLong) context.getBean("atomicLong");
        atomicLong.set(100L);
        System.out.println(atomicLong.get());
    }

    public static void ExecuteAtomicReference()
    {
        System.out.println("### AtomicReference Execution Started ###");
        IAtomicReference atomicReference = (IAtomicReference) context.getBean("atomicReference");
        atomicReference.set("Executing AtomicReference");
        System.out.println(atomicReference.get());
    }

    public static void ExecuteCountDownLatch()
    {
        System.out.println("### CountDownLatch Execution Started ###");
        ICountDownLatch countDownLatch = (ICountDownLatch) context.getBean("countDownLatch");
        countDownLatch.trySetCount(10);
        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());
    }

    public static void ExecuteSemaphore()
    {
        System.out.println("### Semaphore Execution Started ###");
        ISemaphore semaphore = (ISemaphore) context.getBean("semaphore");
        semaphore.init(5);
        try { semaphore.acquire(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println(semaphore.availablePermits());
        semaphore.release();
        System.out.println(semaphore.availablePermits());
    }

    public static void ExecuteLock()
    {
        System.out.println("### Lock Execution Started ###");
        ILock lock = (ILock) context.getBean("lock");
        lock.lock();
        System.out.println(lock.isLockedByCurrentThread());
        lock.unlock();
        System.out.println(lock.isLocked());
    }
}