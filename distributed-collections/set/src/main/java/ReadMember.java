import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;

public class ReadMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ISet<String> set = hz.getSet("set");
        for (String s : set) {
            System.out.println(s);
        }
        System.out.println("Reading finished!");
    }
}
