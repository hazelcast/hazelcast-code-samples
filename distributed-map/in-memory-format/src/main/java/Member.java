import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        IMap<String, Person> binaryMap = instance.getMap("binaryMap");
        IMap<String, Person> objectMap = instance.getMap("objectMap");

        Person person = new Person();
        binaryMap.put("peter", person);
        objectMap.put("peter", person);

        System.out.println(person == binaryMap.get("peter"));
        System.out.println(binaryMap.get("peter") == binaryMap.get("peter"));
        System.out.println(person == objectMap.get("peter"));
        System.out.println(objectMap.get("peter") == objectMap.get("peter"));
    }

    public static class Person implements Serializable {
    }
}
