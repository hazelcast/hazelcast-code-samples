import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;

import java.io.Serializable;

public class WriteMember {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        //ISet<String> set = hz.getSet("set");
        //set.add("Tokyo");
        //set.add("Paris");
        //set.add("London");
        //set.add("New York");
        //System.out.println("Putting finished!");

        ISet x = hz.getSet("x");
        x.add(new Foo(10, 0));
        x.add(new Foo(20, 0));

        System.out.println("---------");
        Foo foo = new Foo(20, 1);
        System.out.println("map.contains: " + x.contains(foo));
    }

    private static class Foo implements Serializable {
        private int x;
        private int z;

        private Foo(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            System.out.println("equals called");
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Foo foo = (Foo) o;

            if (x != foo.x) return false;

            return true;
        }

        @Override
        public int hashCode() {

            System.out.println("hashcode called");
            return x;
        }
    }
}
