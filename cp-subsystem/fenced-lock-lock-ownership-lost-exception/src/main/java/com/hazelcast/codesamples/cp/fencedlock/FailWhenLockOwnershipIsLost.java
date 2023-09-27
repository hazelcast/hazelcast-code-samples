package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPGroup;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.cp.lock.exception.LockOwnershipLostException;
import com.hazelcast.cp.session.CPSession;
import com.hazelcast.cp.session.CPSessionManagementService;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * This code sample demonstrates failure of a reentrant lock() call
 * after CP session of the lock holder is closed in the CP group.
 */
public class FailWhenLockOwnershipIsLost {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        FencedLock lock = hz1.getCPSubsystem().getLock("my-lock");
        lock.lock();

        CPSessionManagementService sessionManagementService = hz2.getCPSubsystem().getCPSessionManagementService();
        Collection<CPSession> sessions = sessionManagementService.getAllSessions(CPGroup.DEFAULT_GROUP_NAME)
                .toCompletableFuture().get();

        assert sessions.size() == 1;
        CPSession session = sessions.iterator().next();
        // There is only one active session and it belongs to the first instance.
        // We are closing its session forcefully to mimic that
        // its session was closed because of missing session heartbeats...
        sessionManagementService.forceCloseSession(CPGroup.DEFAULT_GROUP_NAME, session.id()).toCompletableFuture().get();

        try {
            // The new lock acquire call of the lock holder
            // fails with LockOwnershipLostException
            lock.lock();
            assert false;
        } catch (LockOwnershipLostException expected) {
            System.out.println("The previous lock holder is notified that it has lost ownership of the lock!");
        }

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }

}
