/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.demo;

import com.hazelcast.collection.IQueue;
import com.hazelcast.config.Config;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.diagnostics.HealthMonitorLevel;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * A test of queues, topics, Maps, AtomicInteger etc.
 */
@SuppressWarnings("checkstyle:methodcount")
public final class AllTest {

    private static final int ONE_SECOND = 1000;
    private static final int STATS_SECONDS = 10;
    private static final int SIZE = 10000;

    private static final Logger LOGGER = Logger.getLogger("All-test");

    private final HazelcastInstance hazelcast;
    private final int nThreads;
    private final List<Runnable> operations = new ArrayList<Runnable>();
    private final ExecutorService ex;
    private final Random random = new Random();
    private final AtomicInteger messagesReceived = new AtomicInteger(0);
    private final AtomicInteger messagesSend = new AtomicInteger(0);

    private volatile boolean running = true;

    private AllTest(int nThreads) {
        this.nThreads = nThreads;
        ex = Executors.newFixedThreadPool(nThreads);
        Config config = new Config();
        config.setProperty("hazelcast.health.monitoring.level", HealthMonitorLevel.NOISY.toString());
        config.setProperty("hazelcast.health.monitoring.delay.seconds", Integer.toString(STATS_SECONDS));
        hazelcast = Hazelcast.newHazelcastInstance(config);
        List<Runnable> mapOperations = loadMapOperations();
        List<Runnable> qOperations = loadQOperations();
        List<Runnable> topicOperations = loadTopicOperations();
        this.operations.addAll(mapOperations);
        this.operations.addAll(qOperations);
        this.operations.addAll(topicOperations);
        Collections.shuffle(operations);
    }

    /**
     * Starts the test
     *
     * @param args the number of threads to start
     */
    public static void main(String[] args) {
        int nThreads = (args.length == 0) ? 10 : Integer.parseInt(args[0]);
        final AllTest allTest = new AllTest(nThreads);
        allTest.start();
        Executors.newSingleThreadExecutor().execute(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(STATS_SECONDS * ONE_SECOND);
                        System.out.println("cluster size: " + allTest.hazelcast.getCluster().getMembers().size());
                        allTest.mapStats();
                        allTest.qStats();
                        allTest.topicStats();
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }
        });
    }

    private void qStats() {
        log(hazelcast.getQueue("myQ").getLocalQueueStats());
    }

    private void log(Object message) {
        if (message != null) {
            LOGGER.info(message.toString());
        }
    }

    private void mapStats() {
        log(hazelcast.getMap("myMap").getLocalMapStats());
    }

    private void topicStats() {
        log("Topic Messages Sent: " + messagesSend.getAndSet(0) / STATS_SECONDS
                + "::: Messages Received: " + messagesReceived.getAndSet(0) / STATS_SECONDS);
    }

    private void addOperation(List<Runnable> operations, Runnable runnable, int priority) {
        for (int i = 0; i < priority; i++) {
            operations.add(runnable);
        }
    }

    private void start() {
        for (int i = 0; i < nThreads; i++) {
            ex.submit(new Runnable() {
                public void run() {
                    while (running) {
                        int opId = random.nextInt(operations.size());
                        Runnable operation = operations.get(opId);
                        operation.run();
                    }
                }
            });
        }
    }

    private void stop() {
        running = false;
    }

    /**
     * An example customer class
     */
    @SuppressWarnings("unused")
    public static class Customer implements Serializable {

        private int year;
        private String name;
        private byte[] field = new byte[100];

        Customer(int i, String s) {
            this.year = i;
            this.name = s;
        }
    }

    private List<Runnable> loadTopicOperations() {
        ITopic topic = hazelcast.getTopic("myTopic");
        topic.addMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                messagesReceived.incrementAndGet();
            }
        });
        List<Runnable> operations = new ArrayList<Runnable>();
        addOperation(operations, new Runnable() {
            public void run() {
                ITopic topic = hazelcast.getTopic("myTopic");
                topic.publish(String.valueOf(random.nextInt(100000000)));
                messagesSend.incrementAndGet();
            }
        }, 10);
        return operations;
    }

    @SuppressWarnings("checkstyle:methodlength")
    private List<Runnable> loadQOperations() {
        List<Runnable> operations = new ArrayList<Runnable>();
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.offer(new byte[100]);
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                try {
                    queue.offer(new byte[100], 10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.contains(new byte[100]);
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.isEmpty();
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.size();
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.remove(new byte[100]);
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.remainingCapacity();
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.poll();
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                queue.add(new byte[100]);
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                try {
                    queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                List list = new ArrayList();
                for (int i = 0; i < 10; i++) {
                    list.add(new byte[100]);
                }
                queue.addAll(list);
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IQueue queue = hazelcast.getQueue("myQ");
                List list = new ArrayList();
                queue.drainTo(list);
            }
        }, 1);
        return operations;
    }

    @SuppressWarnings("checkstyle:methodlength")
    private List<Runnable> loadMapOperations() {
        ArrayList<Runnable> operations = new ArrayList<Runnable>();
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.evict(random.nextInt(SIZE));
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                try {
                    map.getAsync(random.nextInt(SIZE)).toCompletableFuture().get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.containsKey(random.nextInt(SIZE));
            }
        }, 2);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.containsValue(new Customer(random.nextInt(100), String.valueOf(random.nextInt(100000))));
            }
        }, 2);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                int key = random.nextInt(SIZE);
                map.lock(key);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    map.unlock(key);
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                int key = random.nextInt(SIZE);
                boolean locked = map.tryLock(key);
                if (locked) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        map.unlock(key);
                    }
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                int key = random.nextInt(SIZE);
                boolean locked = false;
                try {
                    locked = map.tryLock(key, 10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (locked) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        map.unlock(key);
                    }
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.entrySet().iterator();
                for (int i = 0; i < 10 && it.hasNext(); i++) {
                    it.next();
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.getEntryView(random.nextInt(SIZE));
            }
        }, 2);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.isEmpty();
            }
        }, 3);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.put(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
            }
        }, 50);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.tryPut(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))), 10,
                        TimeUnit.MILLISECONDS);
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                try {
                    map.putAsync(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000)))
                    ).toCompletableFuture().get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.put(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))), 10,
                        TimeUnit.MILLISECONDS);
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.putIfAbsent(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))),
                        10, TimeUnit.MILLISECONDS);
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.putIfAbsent(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Map localMap = new HashMap();
                for (int i = 0; i < 10; i++) {
                    localMap.put(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
                }
                map.putAll(localMap);
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.get(random.nextInt(SIZE));
            }
        }, 100);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.remove(random.nextInt(SIZE));
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.tryRemove(random.nextInt(SIZE), 10, TimeUnit.MILLISECONDS);
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.removeAsync(random.nextInt(SIZE));
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.remove(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.replace(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
            }
        }, 4);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.replace(random.nextInt(SIZE), new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))),
                        new Customer(random.nextInt(100), String.valueOf(random.nextInt(10000))));
            }
        }, 5);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.size();
            }
        }, 4);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.entrySet(Predicates.sql("year=" + random.nextInt(100))).iterator();
                while (it.hasNext()) {
                    it.next();
                }
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.entrySet(Predicates.sql("name=" + random.nextInt(10000))).iterator();
                while (it.hasNext()) {
                    it.next();
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.keySet(Predicates.sql("name=" + random.nextInt(10000))).iterator();
                while (it.hasNext()) {
                    it.next();
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.localKeySet().iterator();
                while (it.hasNext()) {
                    it.next();
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                Iterator it = map.localKeySet(Predicates.sql("name=" + random.nextInt(10000))).iterator();
                while (it.hasNext()) {
                    it.next();
                }
            }
        }, 10);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                final CountDownLatch latch = new CountDownLatch(1);
                EntryListener listener = new EntryAdapter() {
                    @Override
                    public void onEntryEvent(EntryEvent event) {
                        latch.countDown();
                    }
                };
                UUID id = map.addEntryListener(listener, true);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                map.removeEntryListener(id);
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                map.addIndex(IndexType.SORTED, "year");
            }
        }, 1);
        addOperation(operations, new Runnable() {
            public void run() {
                IMap map = hazelcast.getMap("myMap");
                final CountDownLatch latch = new CountDownLatch(1);
                EntryListener listener = new EntryAdapter() {
                    @Override
                    public void onEntryEvent(EntryEvent event) {
                        latch.countDown();
                    }
                };
                UUID id = map.addLocalEntryListener(listener);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                map.removeEntryListener(id);
            }
        }, 1);
        return operations;
    }
}
