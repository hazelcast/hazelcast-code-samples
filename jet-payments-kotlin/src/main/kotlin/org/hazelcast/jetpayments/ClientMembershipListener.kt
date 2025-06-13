package org.hazelcast.jetpayments

import com.hazelcast.cluster.MembershipEvent
import com.hazelcast.cluster.MembershipListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/* Gets registered with the Hazelcast cluster, and listens for events indicating
 * that membership has changed.
 */
class ClientMembershipListener(private val originalNumMembers: Int) :
    MembershipListener {
    val numMembersFlow = MutableStateFlow(originalNumMembers)

    override fun memberRemoved(membershipEvent: MembershipEvent?) {
        numMembersFlow.update { it - 1 }
        assert(numMembersFlow.value >= 0) { "${numMembersFlow.value} < 0" }
    }

    override fun memberAdded(membershipEvent: MembershipEvent?) {
        numMembersFlow.update { it + 1 }
        check(numMembersFlow.value <= originalNumMembers) { "${numMembersFlow.value} > $originalNumMembers" }
    }
}
