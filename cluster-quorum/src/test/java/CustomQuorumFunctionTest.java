import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
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

public class CustomQuorumFunctionTest {

    private CustomQuorumFunction quorumFunction = new CustomQuorumFunction();

    @Test
    public void quorumPresent_whenMembersJustAdded() {
        List<Member> members = mockMembers();
        quorumFunction.memberAdded(mockEvent(members.get(0)));
        quorumFunction.memberAdded(mockEvent(members.get(1)));
        assertTrue(quorumFunction.apply(members));
    }

    @Test
    public void quorumPresent_whenHeartbeatsReceivedOnTime()
            throws InterruptedException {
        List<Member> members = mockMembers();
        quorumFunction.memberAdded(mockEvent(members.get(0)));
        quorumFunction.memberAdded(mockEvent(members.get(1)));

        // ensure quorum is not present due to members being just added
        sleep(CustomQuorumFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);

        heartbeat(members, Clock.currentTimeMillis());
        assertTrue(quorumFunction.apply(members));
    }

    @Test
    public void quorumAbsent_whenHeartbeatsLag()
            throws InterruptedException {
        List<Member> members = mockMembers();
        quorumFunction.memberAdded(mockEvent(members.get(0)));
        quorumFunction.memberAdded(mockEvent(members.get(1)));
        // ensure quorum is not present due to members being just added
        sleep(CustomQuorumFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);

        // heartbeat
        heartbeat(members, Clock.currentTimeMillis());

        sleep(CustomQuorumFunction.HEARTBEAT_TOLERANCE_MILLIS * 2);
        // no heartbeat for more than 4 seconds -> quorum is not present
        assertFalse(quorumFunction.apply(members));
    }

    @Test
    public void quorumAbsent_whenNotEnoughMembers() {
        List<Member> members = mockMembers();
        quorumFunction.memberAdded(mockEvent(members.get(0)));
        quorumFunction.memberAdded(mockEvent(members.get(1)));

        List<Member> splitCluster = new ArrayList<Member>();
        splitCluster.add(members.get(0));
        assertFalse(quorumFunction.apply(splitCluster));
    }

    private void heartbeat(List<Member> members, long now) {
        for (Member member : members) {
            quorumFunction.onHeartbeat(member, now);
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
