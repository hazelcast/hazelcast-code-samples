import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spark.connector.HazelcastSparkContext;
import com.hazelcast.spark.connector.rdd.HazelcastJavaRDD;
import domain.User;
import java.util.Arrays;
import java.util.Random;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFlatMapFunction;
import scala.Tuple2;

public class CreateRddFromHazelcast {

    public static void main(String[] args) {
        fillHazelcastMapWithUsers();

        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("Create RDD From Hazelcast")
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("spark.driver.host", "127.0.0.1");

        JavaSparkContext sparkContext = new JavaSparkContext(conf);
        HazelcastSparkContext hazelcastSparkContext = new HazelcastSparkContext(sparkContext);
        HazelcastJavaRDD<String, User> usersRdd = hazelcastSparkContext.fromHazelcastMap("users");

        Double averageAge = usersRdd.flatMapToDouble(new DoubleFlatMapFunction<Tuple2<String, User>>() {
                                                         @Override
                                                         public Iterable<Double> call(Tuple2<String, User> entry) throws Exception {
                                                             return Arrays.asList((double) entry._2().getAge());
                                                         }
                                                     }
        ).mean();
        System.out.println("Average user age = " + averageAge);
    }

    private static void fillHazelcastMapWithUsers() {
        Random random = new Random();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<String, User> users = client.getMap("users");
        for (int i = 0; i < 10; i++) {
            String name = "user-" + i;
            User user = new User(name, random.nextInt(80), random.nextInt(10000));
            users.put(name, user);
        }
        client.getLifecycleService().shutdown();
    }
}
