package hazelcast.platform.labs.machineshop;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import hazelcast.platform.labs.MapWaiter;
import hazelcast.platform.labs.machineshop.domain.MachineProfile;
import hazelcast.platform.labs.machineshop.domain.Names;
import hazelcast.platform.labs.viridian.ViridianConnection;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;

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

//TODO Add Support for additional Kafka connection properties
public class EventGenerator {
    public static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    public static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";
    public static final String KAFKA_BOOTSTRAP_SERVERS_PROP = "KAFKA_BOOTSTRAP_SERVERS";
    public static final String KAFKA_TOPIC_PROP = "KAFKA_TOPIC";

    public static final String MACHINE_COUNT_PROP = "MACHINE_COUNT";

    private static String []hzServers;
    private static String hzClusterName;

    private static int machineCount;
    private static String kafkaBootstrapServers;
    private static String kafkaTopic;

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
        kafkaTopic = getRequiredProp(KAFKA_TOPIC_PROP);
    }


    public static void connectToHazelcast(){
        ClientConfig clientConfig = new ClientConfig();

        if (ViridianConnection.viridianConfigPresent()){
            ViridianConnection.configureFromEnvironment(clientConfig);
        } else {
            clientConfig.setClusterName(hzClusterName);
            for (String server : hzServers) clientConfig.getNetworkConfig().addAddress(server);
        }

        clientConfig.getSerializationConfig().getCompactSerializationConfig()
                .addSerializer(new MachineProfile.Serializer());

        hzClient = HazelcastClient.newHazelcastClient(clientConfig);
        machineProfileMap = hzClient.getMap(Names.PROFILE_MAP_NAME);
        systemActivitiesMap = hzClient.getMap(Names.SYSTEM_ACTIVITIES_MAP_NAME);

        IMap<String, String> machineControlMap = hzClient.getMap(Names.CONTROLS_MAP_NAME);

        // add an event listener on the machine control map
        machineControlMap.addEntryListener(new MachineControlEventHandler(), true);

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

    private static KafkaProducer<String, String> connectToKafka(){
        int PARTITIONS = 23;
        Properties adminClientProps = new Properties();
        adminClientProps.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        try(Admin admin = Admin.create(adminClientProps)){
            ListTopicsResult r = admin.listTopics();
            if (! r.names().get().contains(kafkaTopic)){
                CreateTopicsResult result =
                        admin.createTopics(Collections.singleton(new NewTopic(kafkaTopic, PARTITIONS, (short) 1)));
                result.all().get();
                System.out.println("Created Topic: " + kafkaTopic);
            } else {
                System.out.println("Topic exists: " + kafkaTopic);
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

        KafkaProducer<String, String> producer = connectToKafka();
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

            MachineEmulator emulator = new MachineEmulator(producer, kafkaTopic, sn, signalGen);
            machineEmulatorMap.put(emulator.getSerialNum(), emulator);
            executor.scheduleAtFixedRate(emulator, rand.nextInt(1000), 1000, TimeUnit.MILLISECONDS);
        }
    }

    private static SignalGenerator warmingSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, .8f, 2.0f);
    }

    private static SignalGenerator coolingSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, -1.2f, 2.0f);
    }

    private static SignalGenerator normalSignalGenerator(float startTemp){
        return new SignalGenerator(startTemp, 0.0f, 2.0f);
    }

    public static class MachineControlEventHandler
            implements EntryUpdatedListener<String, String>, EntryAddedListener<String,String> {

        private final Random rand = new Random();

        @Override
        public void entryAdded(EntryEvent<String, String> event) {
            if (event.getValue().equals("red")) handleRed(event.getKey());
        }

        @Override
        public void entryUpdated(EntryEvent<String, String> event) {
            if (event.getValue().equals("green"))
                handleGreen(event.getKey());
            else if (event.getValue().equals("red"))
                handleRed(event.getKey());
        }

        private  void handleRed(String sn){
            System.out.println(sn + " went RED, reducing speed");
            MachineEmulator machine = machineEmulatorMap.get(sn);
            if (machine == null){
                System.err.println("WARNING: No Emulator found for " + sn);
            } else {
                SignalGenerator sg = coolingSignalGenerator(machine.getCurrStatus().getBitTemp());
                machine.setSignalGenerator(sg);
            }
        }

        private  void handleGreen(String sn){
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
}
