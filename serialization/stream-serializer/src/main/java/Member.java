import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, Car> map = hz.getMap("map");

        Person owner = new Person("peter");
        Car car = new Car(owner, "red");

        map.put("mycar", car);
        System.out.println(map.get("mycar"));

        Map<String, ExtendedArrayList> arrayListMap = hz.getMap("arrayListMap");
        ExtendedArrayList extendedArrayList = new ExtendedArrayList();
        extendedArrayList.add(car);
        arrayListMap.put("extended", extendedArrayList);
        ExtendedArrayList deserialized = arrayListMap.get("extended");
        System.out.println(deserialized);

        Hazelcast.shutdownAll();
    }
}
