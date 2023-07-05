import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import org.apache.kafka.connect.source.SourceRecord;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import static com.hazelcast.jet.kafka.connect.KafkaConnectSources.connect;
import static org.apache.kafka.connect.data.Values.convertToString;


/**
 * Demonstrates how to use MySQL as a source for Jet pipelines using Kafka Connect connector
 * <p>
 * Prerequisite: start a MySQL container using the following command:
 * <pre>
 * docker run --rm -it -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_DATABASE=test mysql:8
 * </pre>
 */
public class KafkaConnectMysqlSample {

    public static void main(String[] args) throws Exception {
        // Prepare MySQL table
        executeSQL("DROP TABLE IF EXISTS test_table");
        executeSQL("CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(128))");
        for (int i = 0; i < 10; i++) {
            executeSQL(String.format("INSERT INTO test_table VALUES(%d, 'value-%d');", i, i));
        }

        // Configure the connector properties
        // The properties are described on the connector website:
        // https://docs.confluent.io/kafka-connectors/jdbc/current/source-connector/source_config_options.html#jdbc-source-configs
        Properties properties = new Properties();
        properties.setProperty("name", "my-mysql");
        properties.setProperty("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        properties.setProperty("connection.url", "jdbc:mysql://localhost:3306/test");
        properties.setProperty("connection.user", "root");
        properties.setProperty("connection.password", "mysql");
        properties.setProperty("table.whitelist", "test_table");
        properties.setProperty("table.poll.interval.ms", "1000");
        properties.setProperty("mode", "incrementing");
        properties.setProperty("incrementing.column.name", "id");

        // Define the pipeline
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(connect(properties, (SourceRecord rec) -> convertToString(rec.valueSchema(), rec.value())))
                .withoutTimestamps()
                .writeTo(Sinks.list("items-list"));

        HazelcastInstance hz = startHzInstance();

        // Add connector Jar to the job
        JobConfig jobConfig = new JobConfig();
        //Mirrored from https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc/
        jobConfig.addJarsInZip(new URL("https://repository.hazelcast.com/download"
                                       + "/tests/confluentinc-kafka-connect-jdbc-10.6.3.zip"));

        //Submit the job
        System.out.println("Downloading the connector jar and submitting the job...");
        hz.getJet().newJob(pipeline, jobConfig);
        System.out.println("Job submitted");

        // Wait for data processed by the submitted job
        Thread.sleep(3000);
        System.out.println("Contents of 'items-list': " + new ArrayList<>(hz.getList("items-list")));
    }

    private static HazelcastInstance startHzInstance() {
        Config hzConfig = new Config();
        hzConfig.getJetConfig().setEnabled(true);
        hzConfig.getJetConfig().setResourceUploadEnabled(true);
        return Hazelcast.newHazelcastInstance(hzConfig);
    }

    private static void executeSQL(String sql) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "mysql");
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        }
    }
}
