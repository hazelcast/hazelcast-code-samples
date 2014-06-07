import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;

public class Person implements Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient HazelcastInstance hz;

    public Person(String name) {
        this.name = name;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
        System.out.println("hazelcastInstance set");
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}