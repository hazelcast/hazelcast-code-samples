import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.jet.kafka.connect.KafkaConnectSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import org.apache.kafka.connect.source.SourceRecord;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;


/**
 * Demonstrates how to use Neo4j as a source for Jet pipelines using Kafka Connect connector
 * <p>
 * Prerequisite: start a Neo4j container using the following command:
 * <pre>
 * docker run --rm -e NEO4J_AUTH=none --publish=7474:7474 --publish=7687:7687 neo4j:5.5.0
 * </pre>
 * <p>
 * To generate a new items externally go to <a href="http://localhost:7474/browser/">Neo4j Browser</a>
 * (no authentication required) and execute the following query
 * <pre>
 *     CREATE (:TestSource {name: 'my-name', value: 'my-value', timestamp: datetime().epochMillis})
 * </pre>
 */
public class KafkaConnectNeo4jSample {

    private static final String CONNECTOR_URL = "https://repository.hazelcast.com/download"
                                                + "/tests/neo4j-kafka-connect-neo4j-5.0.2.zip";

    private static final String BOLT_URL = "bolt://localhost:7687";

    public static void main(String[] args) throws Exception {
        Properties connectorProperties = new Properties();
        connectorProperties.setProperty("name", "neo4j");
        connectorProperties.setProperty("connector.class", "streams.kafka.connect.source.Neo4jSourceConnector");
        connectorProperties.setProperty("topic", "some-topic");
        connectorProperties.setProperty("neo4j.server.uri", BOLT_URL);
        connectorProperties.setProperty("neo4j.streaming.poll.interval.msecs", "1000");
        connectorProperties.setProperty("neo4j.streaming.property", "timestamp");
        connectorProperties.setProperty("neo4j.streaming.from", "ALL");
        connectorProperties.setProperty("neo4j.source.query",
                "MATCH (p:Person) WHERE p.timestamp > $lastCheck RETURN p.firstName AS firstName, p.lastName AS lastName,"
                        + " p.role AS role, p.timestamp AS timestamp");

        insertNodes("items-1");

        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(KafkaConnectSources.connect(connectorProperties, Person::from))
                .withoutTimestamps()
                .setLocalParallelism(2)
                .writeTo(Sinks.logger());

        var jobConfig = new JobConfig();
        jobConfig.addJarsInZip(new URL(CONNECTOR_URL));

        Config hzConfig = new Config();
        hzConfig.getJetConfig()
                .setEnabled(true)
                .setResourceUploadEnabled(true);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(hzConfig);
        JetService jet = hz.getJet();
        System.out.println("Downloading the connector jar and submitting the job...");
        var job = jet.newJob(pipeline, jobConfig);
        System.out.println("Job submitted");

        insertNodes("items-2");
        job.join();
    }

    private static void insertNodes(String prefix) {
        try (Driver driver = GraphDatabase.driver(BOLT_URL, AuthTokens.none()); Session session = driver.session()) {
            for (int i = 0; i < 100; i++) {
                session.run("CREATE (:Person {firstName: 'firstName-" + prefix + "-" + i + "', lastName: 'lastName"
                        + prefix + "-" + i + "', role: 'role-" + prefix + "-" + i + "', timestamp: datetime().epochMillis});");
            }
        }
    }

    record Person(String firstName, String lastName) {
        static Person from(SourceRecord rec) {
                try {
                    Map<String, Object> map = JsonUtil.mapFrom(rec.value());
                    assert map != null;
                    return new Person(map.get("firstName").toString(), map.get("lastName").toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
}
