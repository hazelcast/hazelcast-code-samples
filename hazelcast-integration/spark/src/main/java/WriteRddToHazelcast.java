import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;

/**
 * Spark WordCount example writing results to a Hazelcast Map.
 */
public class WriteRddToHazelcast {
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final String INPUT_RESOURCE_NAME = "article";

    public static void main(String[] args) throws Exception {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("Write RDD To Hazelcast")
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("spark.driver.host", "127.0.0.1");

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = ctx.textFile(getFile().getPath(), 1);

        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) {
                return Arrays.asList(SPACE.split(s));
            }
        });

        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        //write counts to the hazelcast map.
        javaPairRddFunctions(counts).saveToHazelcastMap("counts");


        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<Object, Object> countsMap = client.getMap("counts");

        System.out.println("Results fetched from Hazelcast Map :");
        for (Map.Entry<Object, Object> entry : countsMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        client.getLifecycleService().terminate();
        ctx.stop();
    }

    private static File getFile() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new File(classLoader.getResource(INPUT_RESOURCE_NAME).getFile());
    }

}
