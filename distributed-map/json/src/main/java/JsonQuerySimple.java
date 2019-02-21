import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import java.util.Collection;

public class JsonQuerySimple {

    public static void main(String[] args) {

        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        String person1 = "{ \"name\": \"John\", \"age\": 35 }";
        String person2 = "{ \"name\": \"Jane\", \"age\": 24 }";
        String person3 = "{ \"name\": \"Trey\", \"age\": 17 }";
        String person4 = "{ \"name\": \"Phil\", \"age\": 30 }";
        String person5 = "{ \"name\": \"May\"}";

        IMap<Integer, HazelcastJsonValue> idPersonMap = instance.getMap("jsonValues");

        idPersonMap.put(1, new HazelcastJsonValue(person1));
        idPersonMap.put(2, new HazelcastJsonValue(person2));
        idPersonMap.put(3, new HazelcastJsonValue(person3));
        idPersonMap.put(4, new HazelcastJsonValue(person4));
        idPersonMap.put(5, new HazelcastJsonValue(person4));

        Collection<HazelcastJsonValue> peopleUnder21 = idPersonMap.values(Predicates.lessThan("age", 21));

        System.out.println("==> People under 21:");
        for (HazelcastJsonValue personUnder21: peopleUnder21) {
            System.out.println("> " + personUnder21.toString());
        }

        Collection<HazelcastJsonValue> startingWithJ = idPersonMap.values(Predicates.ilike("name", "j%"));

        System.out.println();
        System.out.println("==> People whose names start with J:");
        for (HazelcastJsonValue person: startingWithJ) {
            System.out.println("> " + person.toString());
        }

        instance.shutdown();
    }
}
