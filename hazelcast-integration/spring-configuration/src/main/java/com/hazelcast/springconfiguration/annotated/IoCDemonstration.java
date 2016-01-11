package com.hazelcast.springconfiguration.annotated;

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
import com.hazelcast.core.MultiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * This class shows the way you must declare the attributes to retrieve the hazelcast objects from spring context.
 * <p/>
 * Note: for collections, you must use the @Resource annotation instead of the @Autowired.
 */
@Component
@SuppressWarnings("unused")
public class IoCDemonstration {

    @Resource(name = "map")
    private IMap<Object, Object> hzMap;

    @Autowired
    private MultiMap<Object, Object> hzMultiMap;

    @Resource(name = "queue")
    private IQueue<Object> hzQueue;

    @Autowired
    private ITopic hzTopic;

    @Resource(name = "set")
    private ISet<Object> hzSet;

    @Resource(name = "list")
    private IList<Object> hzList;

    @Autowired
    private IExecutorService hzExecutorService;

    @Autowired
    private IdGenerator hzIdGenerator;

    @Autowired
    private IAtomicLong hzAtomicLong;

    @Autowired
    private IAtomicReference<Object> hzAtomicReference;

    @Autowired
    private ICountDownLatch hzCountDownLatch;

    @Autowired
    private ISemaphore hzSemaphore;

    @Autowired
    private ILock hzLock;

    @PostConstruct
    public void theProof() {
        Assert.notNull(this.hzMap);
        Assert.notNull(this.hzMultiMap);
        Assert.notNull(this.hzQueue);
        Assert.notNull(this.hzTopic);
        Assert.notNull(this.hzSet);
        Assert.notNull(this.hzList);
        Assert.notNull(this.hzExecutorService);
        Assert.notNull(this.hzIdGenerator);
        Assert.notNull(this.hzAtomicLong);
        Assert.notNull(this.hzAtomicReference);
        Assert.notNull(this.hzCountDownLatch);
        Assert.notNull(this.hzSemaphore);
        Assert.notNull(this.hzLock);
        Assert.notNull(this.hzMap);

        System.out.println("hzMap = " + this.hzMap.getClass());
        System.out.println("hzMultiMap = " + this.hzMultiMap.getClass());
        System.out.println("hzQueue = " + this.hzQueue.getClass());
        System.out.println("hzTopic = " + this.hzTopic.getClass());
        System.out.println("hzSet = " + this.hzSet.getClass());
        System.out.println("hzList = " + this.hzList.getClass());
        System.out.println("hzExecutorService = " + this.hzExecutorService.getClass());
        System.out.println("hzIdGenerator = " + this.hzIdGenerator.getClass());
        System.out.println("hzAtomicLong = " + this.hzAtomicLong.getClass());
        System.out.println("hzAtomicReference = " + this.hzAtomicReference.getClass());
        System.out.println("hzCountDownLatch = " + this.hzCountDownLatch.getClass());
        System.out.println("hzSemaphore = " + this.hzSemaphore.getClass());
        System.out.println("hzLock = " + this.hzLock.getClass());
        System.out.println("hzMap = " + this.hzMap.getClass());
    }
}
