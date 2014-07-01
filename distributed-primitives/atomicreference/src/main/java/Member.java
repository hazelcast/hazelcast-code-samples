import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.*;

public class Member {
    public static void main(String[] args) {
        Config config = new Config();

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        IAtomicReference<Double> ref = hz.getAtomicReference("reference");
        ref.compareAndSet(null, new Double(0));
        for (int k = 0; k < 1000 * 1000; k++) {
            if (k % 500000 == 0) {
                System.out.println("At: " + k);
            }
            ref.alter(new IncFuntion());
        }
        System.out.printf("Ref is %s\n", ref.get());
        System.exit(0);
    }

    public static class IncFuntion implements IFunction<Double, Double> {
        @Override
        public Double apply(Double input) {
            return input++;
        }
    }
}