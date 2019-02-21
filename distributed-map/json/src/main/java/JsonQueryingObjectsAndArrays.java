import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import java.util.Collection;

public class JsonQueryingObjectsAndArrays {

    public static void main(String[] args) {

        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        IMap<Integer, HazelcastJsonValue> departments = instance.getMap("departments");

        /**
         * Sample department json object layout
         * {
         *     "departmentId": 1,
         *     "room": "alpha",
         *     "people": [
         *         {
         *             "name": "Peter",
         *             "age": 26,
         *             "salary": 50000
         *         },
         *         {
         *             "name": "Jonah",
         *             "age": 50,
         *             "salary": 140000
         *         }
         *     ]
         * }
         */
        departments.put(1, new HazelcastJsonValue(SampleJsonObjects.DEPARTMENT1));
        departments.put(2, new HazelcastJsonValue(SampleJsonObjects.DEPARTMENT2));

        Collection<HazelcastJsonValue> departmentWithPeter = departments.values(Predicates.equal("people[any].name", "Peter"));

        for (HazelcastJsonValue dep: departmentWithPeter) {
            System.out.println(dep.toString());
        }
        instance.shutdown();
    }
}
