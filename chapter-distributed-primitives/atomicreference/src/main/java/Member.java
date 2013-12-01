import com.hazelcast.core.*;

public class Member {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicReference<Double> ref = hz.getAtomicReference("reference");
        ref.compareAndSet(null,new Double(0));
        for (int k = 0; k < 1000 * 1000; k++) {
            if (k % 500000 == 0) {
                System.out.println("At: " + k);
            }
            ref.alter(new IncFuntion());
        }
        System.out.printf("Ref is %s\n", ref.get());
        System.exit(0);
    }

    public static class IncFuntion implements Function<Double,Double>{
        @Override
        public Double apply(Double input) {
            return input++;
        }
    }
}