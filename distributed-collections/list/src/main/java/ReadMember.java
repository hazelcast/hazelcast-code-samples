import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;

public class ReadMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IList<String> list = hz.getList("list");
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println("Reading finished!");
    }
}
