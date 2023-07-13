package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IFunction;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This code sample demonstrates how monotonic fencing tokens
 * can be used for ordering lock holders and their operations
 * on external services.
 */
public class FencingOffStaleLockHolders {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    /**
     * Contains a "count" value and the fencing token
     * which performed the last increment operation on the value.
     */
    public static class FencedCount implements DataSerializable {

        private long fence;
        private int count;

        public FencedCount() {
        }

        FencedCount(long fence, int count) {
            this.fence = fence;
            this.count = count;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeLong(fence);
            out.writeInt(count);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            fence = in.readLong();
            count = in.readInt();
        }
    }

    /**
     * Increments the current count value if the new fencing token
     * in the function instance is greater than the existing one.
     */
    public static class IncrementIfNotFencedOff implements IFunction<FencedCount, FencedCount>, DataSerializable {

        private long fence;

        public IncrementIfNotFencedOff() {
        }

        IncrementIfNotFencedOff(long fence) {
            this.fence = fence;
        }

        @Override
        public FencedCount apply(FencedCount current) {
            return fence > current.fence ? new FencedCount(fence, current.count + 1) : current;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeLong(fence);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            fence = in.readLong();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        // Our IAtomicReference and FencedLock instances are placed into different CP groups.
        // We can consider them as independent services in practice...
        String atomicRefGroup = "atomicRefs";
        String lockGroup = "locks";

        IAtomicReference<FencedCount> atomicRef = hz1.getCPSubsystem().getAtomicReference("my-ref@" + atomicRefGroup);
        atomicRef.set(new FencedCount(FencedLock.INVALID_FENCE, 0));

        String lockName = "my-lock@" + lockGroup;
        FencedLock hz1Lock = hz1.getCPSubsystem().getLock(lockName);
        FencedLock hz2Lock = hz2.getCPSubsystem().getLock(lockName);

        long fence1 = hz1Lock.lockAndGetFence();
        // The first lock holder increments the count asynchronously.
        Future<Object> future1 = incrementAsync(atomicRef, fence1);
        hz1Lock.unlock();

        long fence2 = hz2Lock.lockAndGetFence();
        // The second lock holder increments the count asynchronously.
        Future<Object> future2 = incrementAsync(atomicRef, fence2);
        hz2Lock.unlock();

        future1.get();
        future2.get();

        // If the increment function of the second holder can be executed before the increment function
        // of the first holder, then the second function execution will not increment the count value.
        int finalValue = atomicRef.get().count;
        assert finalValue == 1 || finalValue == 2;

        EXECUTOR.shutdown();

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        hz1Lock.destroy();
        hz2Lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }

    /**
     * Applies the {@link IncrementIfNotFencedOff} function
     * on the {@link IAtomicReference} instance after some random delay
     */
    private static Future<Object> incrementAsync(final IAtomicReference<FencedCount> atomicRef, final long fence) {
        int randomDelayMs = new Random().nextInt(2000) + 2000;
        return EXECUTOR.schedule(new Callable<Object>() {
            @Override
            public Object call() {
                atomicRef.alter(new IncrementIfNotFencedOff(fence));
                return null;
            }
        }, randomDelayMs, TimeUnit.MILLISECONDS);
    }
}
