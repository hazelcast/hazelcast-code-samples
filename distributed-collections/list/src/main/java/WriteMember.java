import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;

public class WriteMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IList<String> list = hz.getList("list");
        list.add("Tokyo");
        list.add("Paris");
        list.add("London");
        list.add("New York");
        System.out.println("Putting finished!");
    }
}
