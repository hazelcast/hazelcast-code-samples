package net.wrmay.jetdemo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Expects the following environment variables
 * <p>
 * HZ_SERVERS  A comma-separated list of Hazelcast servers in host:port format.  Port may be omitted.
 *             Any whitespace around the commas will be removed.  Required.
 * <p>
 * HZ_CLUSTER_NAME  The name of the Hazelcast cluster to connect.  Required.
 * <p>
 * MACHINE_COUNT The number of machines to load.
 */
public class RefdataLoader {
    public static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    public static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";

    public static final String MACHINE_COUNT_PROP = "MACHINE_COUNT";

    private static String []hzServers;
    private static String hzClusterName;

    private static int machineCount;

    private static final String PROFILE_MAPPING_SQL = "CREATE OR REPLACE MAPPING " + Names.PROFILE_MAP_NAME + " (" +
            "criticalTemp INTEGER, " +
            "manufacturer VARCHAR, " +
            "maxRPM INTEGER, " +
            "serialNum VARCHAR, " +
            "warningTemp INTEGER) " +
            "TYPE IMap OPTIONS (" +
            "'keyFormat' = 'java'," +
            "'keyJavaClass' = 'java.lang.String'," +
            "'valueFormat' = 'compact'," +
            "'valueCompactTypeName' = 'net.wrmay.jetdemo.MachineProfile')";
    private static final String STATUS_MAPPING_SQL = "CREATE OR REPLACE MAPPING " + Names.STATUS_MAP_NAME +
            " TYPE IMap OPTIONS (" +
            "'keyFormat' = 'varchar'," +
            "'valueFormat' = 'varchar')";

    private static String getRequiredProp(String propName){
        String prop = System.getenv(propName);
        if (prop == null){
            System.err.println("The " + propName + " property must be set");
            System.exit(1);
        }
        return prop;
    }

    private static void configure(){
        String hzServersProp = getRequiredProp(HZ_SERVERS_PROP);
        hzServers = hzServersProp.split(",");
        for(int i=0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

        hzClusterName = getRequiredProp(HZ_CLUSTER_NAME_PROP);

        String temp = getRequiredProp(MACHINE_COUNT_PROP);
        try {
            machineCount = Integer.parseInt(temp);
        } catch(NumberFormatException nfx){
            System.err.println("Could not parse " + temp + " as an integer");
            System.exit(1);
        }

        if (machineCount < 1 || machineCount > 1000000){
            System.err.println("Machine count must be between 1 and 1,000,000 inclusive");
            System.exit(1);
        }
    }

    public static void main(String []args){
        configure();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName(hzClusterName);
        clientConfig.getNetworkConfig().addAddress(hzServers);

        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(clientConfig);

        hzClient.getSql().execute(PROFILE_MAPPING_SQL);
        hzClient.getSql().execute(STATUS_MAPPING_SQL);

        Map<String, MachineProfile> batch = new HashMap<>();
        IMap<String, MachineProfile> machineProfileMap = hzClient.getMap(Names.PROFILE_MAP_NAME);

        int existingEntries = machineProfileMap.size();
        int toLoad = machineCount - existingEntries;

        if (toLoad <= 0){
            System.out.println("" + existingEntries + "machine profiles are already present");
        } else {
            for(int i=0; i < toLoad; ++i){
                MachineProfile mp = MachineProfile.fake();
                batch.put(mp.getSerialNum(), mp);
                int BATCH_SIZE = 1000;
                if (batch.size() == BATCH_SIZE){
                    machineProfileMap.putAll(batch);
                    batch.clear();
                }
            }

            if (batch.size() > 0) machineProfileMap.putAll(batch);

            if (machineCount == toLoad)
                System.out.println("Loaded " + machineCount + " machine profiles");
            else
                System.out.println("Loaded " + toLoad + " machine profiles bringing the total to " + machineCount);

            // now add the "special" machine for the demo
            MachineProfile mp = MachineProfile.special();
            machineProfileMap.put(mp.getSerialNum(), mp);
        }

        hzClient.shutdown();
    }
}
