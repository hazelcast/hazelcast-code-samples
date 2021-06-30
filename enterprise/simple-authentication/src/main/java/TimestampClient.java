import java.io.IOException;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class TimestampClient {

    public static void main(String[] args) throws IOException {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<String, Long> map = null;
        while (map == null) {
            try {
                map = client.getMap("timestamps");
            } catch (AccessControlException ae) {
                System.out.println("Unable to work with timestamps map: " + ae);
                sleep();
            }
        }

        try {
            while (true) {
                System.out.print("Reading timestamp: ");
                try {
                    System.out.println(map.get("timestamp"));
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                System.out.print("Setting new timestamp: ");
                try {
                    map.put("timestamp", System.currentTimeMillis());
                    System.out.println("passed");
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                sleep();
            }
        } finally {
            client.shutdown();
        }
    }

    protected static void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
