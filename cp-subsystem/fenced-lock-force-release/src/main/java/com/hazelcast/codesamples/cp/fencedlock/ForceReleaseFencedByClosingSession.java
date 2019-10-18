package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPGroup;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.cp.session.CPSession;
import com.hazelcast.cp.session.CPSessionManagementService;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * This code sample demonstrates that a FencedLock can be released when
 * the CP session of its current holder is closed via the API. We can use this
 * API when we know for sure that the current lock holder is crashed.
 */
public class ForceReleaseFencedByClosingSession {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        hz1.getCPSubsystem().getLock("my-lock").lock();
        // The lock holding Hazelcast instance crashes..
        hz1.getLifecycleService().terminate();

        CPSessionManagementService sessionManagementService = hz2.getCPSubsystem().getCPSessionManagementService();
        Collection<CPSession> sessions = sessionManagementService.getAllSessions(CPGroup.DEFAULT_GROUP_NAME)
                .toCompletableFuture().get();
        // There is only one active session and it belongs to the first instance
        assert sessions.size() == 1;
        CPSession session = sessions.iterator().next();
        // We know that the lock holding instance is crashed.
        // We are closing its session forcefully, hence releasing the lock...
        sessionManagementService.forceCloseSession(CPGroup.DEFAULT_GROUP_NAME, session.id()).toCompletableFuture().get();

        FencedLock lock = hz2.getCPSubsystem().getLock("my-lock");
        assert !lock.isLocked();

        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
