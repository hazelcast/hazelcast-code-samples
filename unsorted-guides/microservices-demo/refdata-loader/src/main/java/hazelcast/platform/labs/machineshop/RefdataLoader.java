package hazelcast.platform.labs.machineshop;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import hazelcast.platform.labs.machineshop.domain.MachineProfile;
import hazelcast.platform.labs.machineshop.domain.MachineStatusSummary;
import hazelcast.platform.labs.machineshop.domain.Names;
import hazelcast.platform.labs.machineshop.domain.StatusServiceResponse;
import hazelcast.platform.labs.viridian.ViridianConnection;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * 
 * SIMULATOR_CONFIG_FILE The path to the simulator configuration
 *
 * We need a way to group machines so that when we try to view a graph, we are only looking at metrics
 * from a subset.  At the same time, we don't want the data to be too sparse. Takes a csv fromatted config file that
 * looks like this.  The fields are location, block and faulty percentage
 * 
 * Los Angeles,A,.9
 * Los Angeles,B,0
 * San Antonio,A,.9
 * San Antonio,B,0
 * 
 */
public class RefdataLoader {
    private static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    private static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";

    private static final String MACHINE_COUNT_PROP = "MACHINE_COUNT";

    private static final String SIMULATOR_CONFIG_FILE_PROP = "SIMULATOR_CONFIG_FILE";

    private static String []hzServers;
    private static String hzClusterName;

    private static int machineCount;

    private static final List<Profile> profiles = new ArrayList<>();

    private static final String PROFILE_MAPPING_SQL = "CREATE OR REPLACE MAPPING " + Names.PROFILE_MAP_NAME + " (" +
            "serialNum VARCHAR, " +
            "location VARCHAR, " +
            "block VARCHAR, " +
            "faultyOdds REAL," +
            "manufacturer VARCHAR, " +
            "warningTemp SMALLINT, " +
            "criticalTemp SMALLINT, " +
            "maxRPM INTEGER) " +
            "TYPE IMap OPTIONS (" +
            "'keyFormat' = 'java'," +
            "'keyJavaClass' = 'java.lang.String'," +
            "'valueFormat' = 'compact'," +
            "'valueCompactTypeName' = 'hazelcast.platform.labs.machineshop.domain.MachineProfile')";
    private static final String CONTROLS_MAPPING_SQL = "CREATE OR REPLACE MAPPING " + Names.CONTROLS_MAP_NAME +
            " TYPE IMap OPTIONS (" +
            "'keyFormat' = 'varchar'," +
            "'valueFormat' = 'varchar')";

    private static final String SYSTEM_ACTIVITIES_MAPPING_SQL = "CREATE OR REPLACE MAPPING " +
            Names.SYSTEM_ACTIVITIES_MAP_NAME +
            " TYPE IMap OPTIONS (" +
            "'keyFormat' = 'varchar'," +
            "'valueFormat' = 'varchar')";

    private static final String MACHINE_PROFILE_LOCATION_INDEX_SQL =
             "CREATE INDEX IF NOT EXISTS MACHINE_LOCATION_INDEX ON " + Names.PROFILE_MAP_NAME + " (location) " +
                     "TYPE HASH;";

    private static final String MACHINE_STATUS_SUMMARY_MAPPING_SQL = "CREATE OR REPLACE MAPPING " + Names.STATUS_SUMMARY_MAP_NAME +
            "( serialNumber VARCHAR, averageBitTemp10s SMALLINT) " +
            "TYPE IMap OPTIONS (" +
            "'keyFormat' = 'java'," +
            "'keyJavaClass' = 'java.lang.String'," +
            "'valueFormat' = 'compact'," +
            "'valueCompactTypeName'='" + MachineStatusSummary.class.getName() + "')";

    private static String getRequiredProp(String propName){
        String prop = System.getenv(propName);
        if (prop == null){
            System.err.println("The " + propName + " property must be set");
            System.exit(1);
        }
        return prop;
    }

    private static void configure(){
        if (!ViridianConnection.viridianConfigPresent()) {
            String hzServersProp = getRequiredProp(HZ_SERVERS_PROP);
            hzServers = hzServersProp.split(",");
            for (int i = 0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

            hzClusterName = getRequiredProp(HZ_CLUSTER_NAME_PROP);
        }

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

        String profileFileName = getRequiredProp(SIMULATOR_CONFIG_FILE_PROP);
        File profileFile = new File(profileFileName);
        if (!profileFile.isFile()){
            System.err.println(profileFileName + " not found");
            System.exit(1);
        }


        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(profileFile)))){
            String line = reader.readLine();
            while(line != null){
                if (line.length() == 0) {
                    line = reader.readLine();
                    continue;
                }

                String []words = line.split(",");
                if (words.length != 3){
                    System.err.println("WARNING: skipping unparseable line: " + line);
                    reader.readLine();
                    continue;
                }

                float pFaulty;
                try {
                    pFaulty = Float.parseFloat(words[2]);
                } catch (NumberFormatException nfx){
                    System.err.println("WARNING: skipping line containing unparseable number: " + line);
                    line = reader.readLine();
                    continue;
                }

                // finally, the error checking is done
                profiles.add(new Profile(words[0], words[1], pFaulty));
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading " + profileFileName);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void doSQLMappings(HazelcastInstance hzClient){
        hzClient.getSql().execute(PROFILE_MAPPING_SQL);
        hzClient.getSql().execute(CONTROLS_MAPPING_SQL);
        hzClient.getSql().execute(SYSTEM_ACTIVITIES_MAPPING_SQL);
        hzClient.getSql().execute(MACHINE_STATUS_SUMMARY_MAPPING_SQL);
        hzClient.getSql().execute(MACHINE_PROFILE_LOCATION_INDEX_SQL);
        System.out.println("Initialized SQL Mappings");
    }

    private static void configureMaps(HazelcastInstance hzClient){
        hzClient.getConfig().addMapConfig(new MapConfig(Names.PROFILE_MAP_NAME)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setBackupCount(1));

        System.out.println("Initialized Map Configurations");
    }
    public static void main(String []args){
        configure();

        ClientConfig clientConfig = new ClientConfig();
        if (ViridianConnection.viridianConfigPresent()){
            ViridianConnection.configureFromEnvironment(clientConfig);
        } else {
            clientConfig.setClusterName(hzClusterName);
            clientConfig.getNetworkConfig().addAddress(hzServers);
        }
        clientConfig.getConnectionStrategyConfig().setAsyncStart(false);
        clientConfig.getConnectionStrategyConfig().setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ON);
        clientConfig.getSerializationConfig().getCompactSerializationConfig()
                .addSerializer(new MachineStatusSummary.Serializer())
                .addSerializer(new MachineProfile.Serializer())
                .addSerializer(new StatusServiceResponse.Serializer());

        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(clientConfig);

        if (ViridianConnection.viridianConfigPresent()){
            configureMaps(hzClient);
        }
        doSQLMappings(hzClient);

        Map<String, MachineProfile> batch = new HashMap<>();
        IMap<String, MachineProfile> machineProfileMap = hzClient.getMap(Names.PROFILE_MAP_NAME);
        IMap<String, String> systemActivitiesMap = hzClient.getMap(Names.SYSTEM_ACTIVITIES_MAP_NAME);

        systemActivitiesMap.put("LOADER_STATUS","STARTED");

        int existingEntries = machineProfileMap.size();
        int toLoad = machineCount - existingEntries;

        if (toLoad <= 0){
            System.out.println("" + existingEntries + " machine profiles are already present");
        } else {
            for(int i=0; i < toLoad; ++i){
                Profile p = profiles.get( i % profiles.size());
                MachineProfile mp = MachineProfile.fake(p.location, p.block, p.faultyPercentage);
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
        }
        systemActivitiesMap.put("LOADER_STATUS","FINISHED");
        hzClient.shutdown();
    }

    private static class Profile {
        String location;
        String block;
        float faultyPercentage;

        public Profile(String location, String block, float faultyPercentage) {
            this.location = location;
            this.block = block;
            this.faultyPercentage = faultyPercentage;
        }
    }

}
