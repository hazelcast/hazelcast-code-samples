import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.util.Clock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomSplitBrainProtectionFunctionTest {

    private CustomSplitBrainProtectionFunction splitBrainProtectionFunction = new CustomSplitBrainProtectionFunction();

    @Test
    public void splitBrainProtectionPresent_whenMembersJustAdded() {
        List<Member> members = mockMembers();
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(0)));
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(1)));
        assertTrue(splitBrainProtectionFunction.apply(members));
    }

    @Test
    public void splitBrainProtectionPresent_whenHeartbeatsReceivedOnTime()
            throws InterruptedException {
        List<Member> members = mockMembers();
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(0)));
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(1)));

        // ensure split brain protection is not present due to members being just added
        sleep(CustomSplitBrainProtectionFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);

        heartbeat(members, Clock.currentTimeMillis());
        assertTrue(splitBrainProtectionFunction.apply(members));
    }

    @Test
    public void splitBrainProtectionAbsent_whenHeartbeatsLag()
            throws InterruptedException {
        List<Member> members = mockMembers();
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(0)));
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(1)));
        // ensure split brain protection is not present due to members being just added
        sleep(CustomSplitBrainProtectionFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);

        // heartbeat
        heartbeat(members, Clock.currentTimeMillis());

        sleep(CustomSplitBrainProtectionFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);
        // no heartbeat for more than 4 seconds -> split brain protection is not present
        assertFalse(splitBrainProtectionFunction.apply(members));
    }

    @Test
    public void splitBrainProtectionAbsent_whenNotEnoughMembers() {
        List<Member> members = mockMembers();
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(0)));
        splitBrainProtectionFunction.memberAdded(mockEvent(members.get(1)));

        List<Member> splitCluster = new ArrayList<Member>();
        splitCluster.add(members.get(0));
        assertFalse(splitBrainProtectionFunction.apply(splitCluster));
    }

    private void heartbeat(List<Member> members, long now) {
        for (Member member : members) {
            splitBrainProtectionFunction.onHeartbeat(member, now);
        }
    }

    private MembershipEvent mockEvent(Member member) {
        MembershipEvent event = mock(MembershipEvent.class);
        when(event.getMember()).thenReturn(member);
        return event;
    }

    private Member mockMember() {
        return mock(Member.class);
    }

    private List<Member> mockMembers() {
        return asList(new Member[] {mockMember(), mockMember()});
    }

}
