import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static com.hazelcast.jet.aggregate.AggregateOperations.averagingDouble;
import static com.hazelcast.jet.kafka.connect.KafkaConnectSources.connect;


/**
 * Demonstrates how to use Redis as a source for Jet pipelines using Kafka Connect connector.
 * Scenario is simple - some service is posting weather info to Redis, our Jet pipeline watches those changes
 * and calculate average for this city.
 *
 * <p>
 * Prerequisite: start a Redis container using the following command:
 * <pre>
 * docker run --rm -d --name test-redis --publish=6379:6379 redis:7
 * </pre>
 *
 * then execute following command:
 * <pre> docker exec -it test-redis redis-cli CONFIG SET notify-keyspace-events KEA</pre>
 *
 * <p>
 * To run Redis CLI you can execute:
 * <pre>docker exec -it test-redis redis-cli</pre>
 * and run for example:
 * <pre>
 *     SET someKey "someValue"  `
 * </pre>
 */
public class KafkaConnectRedisSample {

    private static final String CONNECTOR_URL
            = "https://repository.hazelcast.com/download/tests/redis-redis-kafka-connect-0.9.0.zip";

    private static final List<String> CITIES = List.of("Wrocław", "Warszawa", "Białystok", "Brno", "Prague",
            "Paris", "London", "Ankara", "Rome", "Madrit", "Washington DC");

    private static final String REDIS_URI = System.getProperty("REDIS_URI", "redis://localhost:6379");

    public static void main(String[] args) throws Exception {
        Properties connectorProperties = new Properties();
        connectorProperties.putAll(Map.of(
                "name", "redis",
                "connector.class", "com.redis.kafka.connect.RedisKeysSourceConnector",
                "redis.uri", REDIS_URI,
                "redis.keys.pattern", "*",
                "topic", "some-topic"
        ));

        var pipeline = Pipeline.create();
        pipeline.readFrom(connect(connectorProperties, CityTemp::parse))
                .withIngestionTimestamps()
                .setLocalParallelism(2)

                .groupingKey(CityTemp::city)
                .rollingAggregate(averagingDouble(CityTemp::temperature))

                .writeTo(Sinks.logger());

        JobConfig jobConfig = new JobConfig();
        jobConfig.addJarsInZip(new URL(CONNECTOR_URL));

        var hzConfig = new Config();
        hzConfig.setProperty("hazelcast.logging.type", "log4j2");
        hzConfig.getJetConfig()
                .setEnabled(true)
                .setResourceUploadEnabled(true);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(hzConfig);
        JetService jet = hz.getJet();

        System.out.println("Downloading the connector jar and submitting the job...");
        Job job = jet.newJob(pipeline, jobConfig);
        System.out.println("Job submitted");

        new Thread(KafkaConnectRedisSample::insertData).start();

        job.join();
    }

    @SuppressWarnings("BusyWait")
    private static void insertData() {
        Thread currentThread = Thread.currentThread();
        try (RedisClient client = RedisClient.create(REDIS_URI);
             StatefulRedisConnection<String, String> connection = client.connect()) {

            RedisCommands<String, String> syncCommands = connection.sync();

            Random r = new Random();
            while (!currentThread.isInterrupted()) {
                String city = CITIES.get(r.nextInt(CITIES.size()));
                syncCommands.set(city, String.valueOf(r.nextDouble(35.0d)));

                Thread.sleep(r.nextInt(20) * 10);
            }
        } catch (InterruptedException e) {
            currentThread.interrupt();
            throw new RuntimeException(e);
        }
    }

    public record CityTemp(String city, double temperature) {
        public static CityTemp parse(SourceRecord record) {
            var v = (Struct) record.value();
            return new CityTemp(v.getString("key"), Double.parseDouble(v.getString("string")));
        }
    }

}
