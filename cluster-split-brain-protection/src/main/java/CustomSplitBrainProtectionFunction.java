import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.internal.util.Clock;
import com.hazelcast.splitbrainprotection.HeartbeatAware;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

/**
 * This is a custom split brain protection function that tracks membership changes and heartbeats.
 * If a heartbeat has not been received from a member within the last 2 seconds, the member is considered
 * absent for the purposes of split-brain protection. At least 2 members must be present for split brain protection to be
 * considered present. This is a simplified version of built-in
 * {@link com.hazelcast.splitbrainprotection.impl.RecentlyActiveSplitBrainProtectionFunction}.
 */
public class CustomSplitBrainProtectionFunction implements SplitBrainProtectionFunction, MembershipListener, HeartbeatAware {

    static final long HEARTBEAT_TOLERANCE_MILLIS = 3000;
    static final int SPLIT_BRAIN_PROTECTION_SIZE = 2;

    private Map<Member, Long> lastHeartbeatReceived = new HashMap<>();

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        // initialize heartbeat for newly-joined members
        long now = Clock.currentTimeMillis();
        lastHeartbeatReceived.put(membershipEvent.getMember(), now);
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        lastHeartbeatReceived.remove(membershipEvent.getMember());
    }

    @Override
    public void onHeartbeat(Member member, long timestamp) {
        lastHeartbeatReceived.put(member, timestamp);
    }

    @Override
    public boolean apply(Collection<Member> members) {
        int countOfPresentMembers = 0;
        long now = Clock.currentTimeMillis();
        for (Member member : members) {
            Long lastHeartbeatTime = lastHeartbeatReceived.get(member);
            if (lastHeartbeatTime == null) {
                continue;
            }
            if (abs(now - lastHeartbeatTime) < HEARTBEAT_TOLERANCE_MILLIS) {
                countOfPresentMembers++;
            }
        }
        return countOfPresentMembers >= SPLIT_BRAIN_PROTECTION_SIZE;
    }
}
