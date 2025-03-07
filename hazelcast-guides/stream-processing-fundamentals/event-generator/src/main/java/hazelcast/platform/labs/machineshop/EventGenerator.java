package hazelcast.platform.labs.machineshop;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import hazelcast.platform.labs.MapWaiter;
import hazelcast.platform.labs.machineshop.domain.MachineProfile;
import hazelcast.platform.labs.machineshop.domain.Names;
import hazelcast.platform.labs.viridian.ViridianConnection;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Expects the following environment variables
 * <p>
 * HZ_SERVERS  A comma-separated list of Hazelcast servers in host:port format.  Port may be omitted.
 * Any whitespace around the commas will be removed.  Required.
 * <p>
 * HZ_CLUSTER_NAME  The name of the Hazelcast cluster to connect.  Required.
 * <p>
 * MACHINE_COUNT The number of machines to emulate.
 * <p>
 * KAFKA_BOOTSTRAP_SERVERS   Provides the bootstrap.servers value to the KafkaProducer api
 * <p>
 * KAFKA_TOPIC               The name of the topic to which events will be published
 */
public class EventGenerator {
    public static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    public static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";
    public static final String KAFKA_BOOTSTRAP_SERVERS_PROP = "KAFKA_BOOTSTRAP_SERVERS";
    public static final String EVENTS_KAFKA_TOPIC_PROP = "EVENTS_KAFKA_TOPIC";
    public static final String CONTROLS_KAFKA_TOPIC_PROP = "CONTROLS_KAFKA_TOPIC";

    public static final String MACHINE_COUNT_PROP = "MACHINE_COUNT";

    private static String []hzServers;
    private static String hzClusterName;

    private static int machineCount;
    private static String kafkaBootstrapServers;
    private static String eventsKafkaTopic;
    private static String controlsKafkaTopic;

    private static Set<String> serialNums;

    private static  HazelcastInstance hzClient;
    private static IMap<String, MachineProfile> machineProfileMap;
    private static IMap<String, String> systemActivitiesMap;
    private static final ConcurrentHashMap<String, MachineEmulator> machineEmulatorMap = new ConcurrentHashMap<>();

    private static String getRequiredProp(String propName) {
        String prop = System.getenv(propName);
        if (prop == null) {
            System.err.println("The " + propName + " property must be set");
            System.exit(1);
        }
        return prop;
    }

    // guarantees to return a result or call System.exit
    private static int getRequiredIntegerProp(String propName) {
        String temp = getRequiredProp(propName);
        int result = 0;
        try {
            result = Integer.parseInt(temp);
        } catch (NumberFormatException nfx) {
            System.err.println("Could not parse " + temp + " as an integer");
            System.exit(1);
        }

        return result;
    }

    private static void configure() {
        if (!ViridianConnection.viridianConfigPresent()){
            String hzServersProp = getRequiredProp(HZ_SERVERS_PROP);
            hzServers = hzServersProp.split(",");
            for (int i = 0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

            hzClusterName = getRequiredProp(HZ_CLUSTER_NAME_PROP);
        }

        machineCount = getRequiredIntegerProp(MACHINE_COUNT_PROP);

        if (machineCount < 1 || machineCount > 1000000) {
            System.err.println("Machine count must be between 1 and 1,000,000 inclusive");
            System.exit(1);
        }

        kafkaBootstrapServers = getRequiredProp(KAFKA_BOOTSTRAP_SERVERS_PROP);
        eventsKafkaTopic = getRequiredProp(EVENTS_KAFKA_TOPIC_PROP);
        controlsKafkaTopic = getRequiredProp(CONTROLS_KAFKA_TOPIC_PROP);
    }


    public static void connectToHazelcast(){
        ClientConfig clientConfig = new ClientConfig();

        if (ViridianConnection.viridianConfigPresent()){
            ViridianConnection.configureFromEnvironment(clientConfig);
        } else {
            clientConfig.setClusterName(hzClusterName);
            for (String server : hzServers) clientConfig.getNetworkConfig().addAddress(server);
        }

        hzClient = HazelcastClient.newHazelcastClient(clientConfig);
        machineProfileMap = hzClient.getMap(Names.PROFILE_MAP_NAME);
        systemActivitiesMap = hzClient.getMap(Names.SYSTEM_ACTIVITIES_MAP_NAME);

        Runtime.getRuntime().addShutdownHook(new Thread(hzClient::shutdown));
    }
    /**
     * This routine connects to Hazelcast as a client, waits for there to be machineCount entries in
     * the machine profile map, then selects that many serial numbers into the serialNums static
     * array.
     */
    private static void retrieveSerialNumbersFromHz(){
        MapWaiter<String, String> waiter = new MapWaiter<>(systemActivitiesMap, "LOADER_STATUS", "FINISHED");
        if (!waiter.waitForSignal(3*60*1000)){
           System.err.println("Timed out waiting for reference data loader to complete");
           System.exit(1);
        }
        System.out.println("Reference data loader has finished.  Proceeding");

        // now we have sufficient profiles to start generating data
        serialNums = machineProfileMap.keySet();
        if (serialNums.size() < machineCount){
            System.err.println("Could not retrieve sufficient profiles from the " + Names.PROFILE_MAP_NAME + " map.");
            System.exit(1);
        }
    }

    private static KafkaProducer<String, String> connectToKafkaSink(){
        int PARTITIONS = 23;
        Properties adminClientProps = new Properties();
        adminClientProps.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        try(Admin admin = Admin.create(adminClientProps)){
            ListTopicsResult r = admin.listTopics();
            if (! r.names().get().contains(eventsKafkaTopic)){
                CreateTopicsResult result =
                        admin.createTopics(Collections.singleton(new NewTopic(eventsKafkaTopic, PARTITIONS, (short) 1)));
                result.all().get();
                System.out.println("Created Topic: " + eventsKafkaTopic);
            } else {
                System.out.println("Topic exists: " + eventsKafkaTopic);
            }
        } catch (ExecutionException | InterruptedException ee) {
            ee.printStackTrace();
            System.exit(1);
        }

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaBootstrapServers);
        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    private static KafkaConsumer<String, String> connectToKafkaSource(){
        int PARTITIONS = 23;
        Properties adminClientProps = new Properties();
        adminClientProps.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        try(Admin admin = Admin.create(adminClientProps)){
            ListTopicsResult r = admin.listTopics();
            if (! r.names().get().contains(controlsKafkaTopic)){
                CreateTopicsResult result =
                        admin.createTopics(Collections.singleton(new NewTopic(controlsKafkaTopic, PARTITIONS, (short) 1)));
                result.all().get();
                System.out.println("Created Topic: " + controlsKafkaTopic);
            } else {
                System.out.println("Topic exists: " + controlsKafkaTopic);
            }
        } catch (ExecutionException | InterruptedException ee) {
            ee.printStackTrace();
            System.exit(1);
        }

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkaBootstrapServers);
        props.setProperty("group.id", "event-generators");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "4000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);
    }

    public static void main(String[] args) {
        configure();
        connectToHazelcast();
        System.out.println("Connected to Hazelcast");
        System.out.println("Waiting for machine profile data to be ready");
        retrieveSerialNumbersFromHz();
        System.out.println("Starting " + machineCount + " emulators");

        Random rand = new Random();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(16);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));

        KafkaProducer<String, String> producer = connectToKafkaSink();
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));

        for (String sn: serialNums) {
            MachineProfile profile = machineProfileMap.get(sn);
            if (profile == null) {
                System.err.println("Error: profile not found for " + sn + " skipping this emulator");
                continue;
            }

            SignalGenerator signalGen;
            float p = rand.nextFloat();
            if (p <= profile.getFaultyOdds()) {
                // it's faulty
                signalGen = warmingSignalGenerator(.7f * profile.getWarningTemp());
            } else {
                signalGen = normalSignalGenerator(.7f * profile.getWarningTemp());
            }

            MachineEmulator emulator = new MachineEmulator(producer, eventsKafkaTopic, sn, signalGen);
            machineEmulatorMap.put(emulator.getSerialNum(), emulator);
            executor.scheduleAtFixedRate(emulator, rand.nextInt(1000), 1000, TimeUnit.MILLISECONDS);
        }

        // now start polling the controls topic
        try(KafkaConsumer<String,String> consumer = connectToKafkaSource()){
          consumer.subscribe(Collections.singleton(controlsKafkaTopic));
          while(true){
              ConsumerRecords<String, String> records = consumer.poll(Duration.of(4, ChronoUnit.SECONDS));
              for (ConsumerRecord<String,String> r: records){
                  String sn = r.key();
                  String status = r.value();
                  if (status.equals("green")){
                      handleGreen(sn);
                  } else if (status.equals("red")){
                      handleRed(sn);
                  }
              }

          }
        }

    }

    private static Random rand = new Random();

    private static SignalGenerator warmingSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, .8f, 2.0f);
    }

    private static SignalGenerator coolingSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, -1.2f, 2.0f);
    }

    private static SignalGenerator normalSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, 0.0f, 2.0f);
    }

    private  static void handleRed(String sn){
        System.out.println(sn + " went RED, reducing speed");
        MachineEmulator machine = machineEmulatorMap.get(sn);
        if (machine == null){
            System.err.println("WARNING: No Emulator found for " + sn);
        } else {
            SignalGenerator sg = coolingSignalGenerator(machine.getCurrStatus().getBitTemp());
            machine.setSignalGenerator(sg);
        }
    }

    private  static void handleGreen(String sn){
        System.out.println(sn + " went GREEN, resuming normal speed");
        MachineEmulator machine = machineEmulatorMap.get(sn);
        if (machine == null){
            System.err.println("WARNING: No Emulator found for " + sn);
        } else {
            MachineProfile profile = machineProfileMap.get(sn);
            float pFaulty = .1f;
            if (profile != null){
                pFaulty = profile.getFaultyOdds();
            } else {
                System.err.println("WARNING: No machine profile found for: " + sn);
            }

            if (rand.nextFloat() <= pFaulty)
                machine.setSignalGenerator(warmingSignalGenerator(machine.getCurrStatus().getBitTemp()));
            else
                machine.setSignalGenerator(normalSignalGenerator(machine.getCurrStatus().getBitTemp()));
        }
    }
}
