import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.*;

public class Member {
    public static void main(String[] args) {
        MapConfig mapConfig = new MapConfig("foo");
        mapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);

        Config config=new Config();
        config.addMapConfig(mapConfig);


        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        String value = "bla";
        IMap map = hz.getMap(mapConfig.getName());
        map.put("1",value);

        System.out.println("value == map.get:"+(value==map.get("1")));
        System.out.println("map.get == map.get:"+(map.get("1")==map.get("1")));
        if(true){
           return;
       }


        IAtomicReference <Double> ref = hz.getAtomicReference("reference");
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