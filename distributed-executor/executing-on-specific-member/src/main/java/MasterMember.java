import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class MasterMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IExecutorService executorService = hz.getExecutorService("executor");

        for (Member member : hz.getCluster().getMembers()) {
            EchoTask task = new EchoTask(member.getSocketAddress().toString());
            executorService.executeOnMember(task, member);
        }
    }
}
