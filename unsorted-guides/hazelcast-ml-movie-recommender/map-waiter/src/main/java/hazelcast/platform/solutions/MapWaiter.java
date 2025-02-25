package hazelcast.platform.solutions;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MapWaiter simply waits for a specified signal on a specified topic and then
 * exits when the signal is received.  It expects the following environment variables.
 *
 * HZ_CLUSTER_NAME
 * HZ_MEMBERS
 * AWAIT_MAP
 * AWAIT_KEY
 * AWAIT_VALUE
 */
public class MapWaiter {

    public static String getRequireEnvVar(String name){
        String result = System.getenv(name);
        if (result == null){
            System.err.println( "\"" + name + "\" environment variable is required." );
            System.exit(1);
        }
        return result;
    }
    public static void main(String []args){
        String clusterName = getRequireEnvVar("HZ_CLUSTER_NAME");
        String clusterMembers = getRequireEnvVar("HZ_MEMBERS");
        String awaitMapName = getRequireEnvVar("AWAIT_MAP");
        String awaitKey = getRequireEnvVar("AWAIT_KEY");
        String awaitValue = getRequireEnvVar("AWAIT_VALUE");
        System.out.println("Wait for " + awaitKey + "=" + awaitValue + " in " + awaitMapName + " map");

        String []clusterMemberArray = clusterMembers.split(",");

        ClientConfig config = new ClientConfig();
        config.setClusterName(clusterName);
        for(String member: clusterMemberArray){
            config.getNetworkConfig().addAddress(member);
        }
        config.getConnectionStrategyConfig().setAsyncStart(false);
        config.getConnectionStrategyConfig().setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ON);
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(config);
        IMap<String,String> awaitMap = hz.getMap(awaitMapName);
        AtomicBoolean done = new AtomicBoolean(false);  // used for signaling purposes
        awaitMap.addEntryListener(new AwaitedEntryListener(done, awaitKey, awaitValue), awaitKey, true);
        String val = awaitMap.get(awaitKey);
        int exitStatus = 1;
        if (val != null && val.equals(awaitValue)){
            System.out.println("Awaited value is present.");
            exitStatus = 0;
        } else {
            synchronized (done){
                try {
                    System.out.println("Waiting...");
                    done.wait(3 * 60 * 1000);
                    if (done.get()){
                        exitStatus = 0;
                        System.out.println("Awaited key arrived.");
                    } else {
                        exitStatus = 1;
                        System.out.println("Awaited key never arrived. Wait timed out.");
                    }
                } catch(InterruptedException ix){
                    // this is main, we will be exiting either way
                    System.out.println("Interrupted while waiting.");
                    exitStatus = 1;
                }
            }
        }
        System.exit(exitStatus);
    }

    public static class AwaitedEntryListener implements EntryAddedListener<String,String>, EntryUpdatedListener<String, String> {

        final AtomicBoolean monitor;
        String awaitedKey;
        String awaitedValue;
        public AwaitedEntryListener(AtomicBoolean monitor, String awaitedKey, String awaitedValue){
            this.monitor = monitor;
            this.awaitedKey = awaitedKey;
            this.awaitedValue = awaitedValue;
        }

        @Override
        public void entryAdded(EntryEvent<String, String> event) {
            if (event.getKey().equals(awaitedKey) && event.getValue().equals(awaitedValue)){
                synchronized (monitor) {
                    monitor.set(true);
                    monitor.notify();
                }
            } else {
                System.out.println("Received non-matching event: " + event.getKey() + "=" + event.getValue());
            }
        }

        @Override
        public void entryUpdated(EntryEvent<String, String> event) {
            if (event.getKey().equals(awaitedKey) && event.getValue().equals(awaitedValue)){
                synchronized (monitor) {
                    monitor.set(true);
                    monitor.notify();
                }
            } else {
                System.out.println("Received non-matching event: " + event.getKey() + "=" + event.getValue());
            }
        }
    }
}
