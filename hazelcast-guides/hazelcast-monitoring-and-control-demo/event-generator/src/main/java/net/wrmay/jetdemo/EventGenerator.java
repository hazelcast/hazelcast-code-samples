package net.wrmay.jetdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static net.wrmay.jetdemo.Names.SPECIAL_SN;

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
 * TARGET_URL   The URL to which events will be delivered
 */
public class EventGenerator {
    public static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    public static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";

    public static final String MACHINE_COUNT_PROP = "MACHINE_COUNT";

    public static final String RUN_HOT_PROP = "RUNHOT";

    public static final String TARGET_URL_PROP = "TARGET_URL";

    private static String[] hzServers;
    private static String hzClusterName;

    private static int machineCount;

    private static boolean runHot;

    private static String targetURL;

    private static String[] serialNums;

    private static ObjectMapper objectMapper;

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
        String hzServersProp = getRequiredProp(HZ_SERVERS_PROP);
        hzServers = hzServersProp.split(",");
        for (int i = 0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

        hzClusterName = getRequiredProp(HZ_CLUSTER_NAME_PROP);

        machineCount = getRequiredIntegerProp(MACHINE_COUNT_PROP);

        if (machineCount < 1 || machineCount > 1000000) {
            System.err.println("Machine count must be between 1 and 1,000,000 inclusive");
            System.exit(1);
        }

        String str = System.getenv(RUN_HOT_PROP);
        if (str == null) {
            runHot = false;
        } else {
            str = str.toLowerCase();
            runHot = str.equals("yes") || str.equals("true");
        }

        targetURL = getRequiredProp(TARGET_URL_PROP).toLowerCase();
        if (!targetURL.startsWith("http://") && !targetURL.startsWith("https://")) {
            System.err.println("Target URL must start with \"http://\" or \"https://\"");
            System.exit(1);
        }

        objectMapper = new ObjectMapper();
    }


    /**
     * This routine connect to Hazelcast as a client, waits for there to be machineCount entries in
     * the machine profile map, then selects that many serial numbers into the serialNums static
     * array.
     */
    private static void retrieveSerialNumbersFromHz(){
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName(hzClusterName);
        clientConfig.getNetworkConfig().addAddress(hzServers);

        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(clientConfig);
        try (Closer<HazelcastInstance> hzCloser = new Closer<>(hzClient, HazelcastInstance::shutdown)) {
            IMap<String, MachineProfile> machineProfileMap = hzClient.getMap(Names.PROFILE_MAP_NAME);

            int existingEntries = machineProfileMap.size();

            while (existingEntries < machineCount) {
                System.out.println("waiting for at least " + machineCount + " machine profiles to be loaded");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException x) {
                    // ?
                }
                existingEntries = machineProfileMap.size();
            }

            // add some sleep to prevent the condition where the loader has not finished initializing the sql mapping
            try {
                Thread.sleep(5000);
            } catch (InterruptedException x) {
                //
            }

            // now we have sufficient profiles to start generating data
            serialNums = new String[machineCount];
            try (SqlResult result = hzClient.getSql().execute("SELECT serialNum FROM " + Names.PROFILE_MAP_NAME + " WHERE serialNum != ? LIMIT ?", SPECIAL_SN, machineCount)) {
                int i = 0;
                for (SqlRow row : result) {
                    serialNums[i++] = row.getObject(0);
                }
                if (i < machineCount) {
                    System.err.println("Could not retrieve sufficient profiles from the " + Names.PROFILE_MAP_NAME + " map.");
                    System.exit(1);
                }
            }
        } // close the Hazelcast instance
    }
    public static void main(String[] args) {
        configure();


        retrieveSerialNumbersFromHz();

        Random rand = new Random();

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(machineCount / 100, new DaemonThreadFactory());
            try (Closer<ScheduledThreadPoolExecutor> threadPoolExecutorCloser = new Closer<>(executor, ScheduledThreadPoolExecutor::shutdown)) {

                if (runHot) {
                    executor.scheduleAtFixedRate(new MachineEmulator(httpClient, targetURL, SPECIAL_SN, true, objectMapper), 0, 1000, TimeUnit.MILLISECONDS);
                } else {
                    MachineEmulator[] machineEmulators = new MachineEmulator[machineCount];
                    for (int j = 0; j < machineCount; ++j) {
                        machineEmulators[j] = new MachineEmulator(httpClient, targetURL, serialNums[j], false, objectMapper);
                        executor.scheduleAtFixedRate(machineEmulators[j], rand.nextInt(1000), 1000, TimeUnit.MILLISECONDS);
                    }
                }

                AtomicBoolean running = new AtomicBoolean(true);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));

                while (running.get()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ix) {
                        break;
                    }
                }
                System.out.println("Shutting down");
            } // close thread pool
        } catch(IOException iox){  // close the http connection
            System.err.println("There was a problem with the http connection to: " + targetURL);
            iox.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static class Closer<T> implements AutoCloseable {

        private final T client;
        private final Consumer<T> closeFn;

        public Closer(T hc, Consumer<T> closeFn) {
            this.client = hc;
            this.closeFn = closeFn;
        }

        @Override
        public void close() {
            closeFn.accept(client);
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread result = new Thread(r);
            result.setDaemon(true);
            return result;
        }
    }
}
