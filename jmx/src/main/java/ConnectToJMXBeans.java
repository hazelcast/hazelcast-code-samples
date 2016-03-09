import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

public class ConnectToJMXBeans {

    private static final int PORT = 9999;

    public static void main(String[] args) throws Exception {
        // parameters for connecting to the JMX Service
        String hostname = InetAddress.getLocalHost().getHostName();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + PORT
                + "/jndi/rmi://" + hostname + ":" + PORT + "/jmxrmi");

        // starting a Hazelcast member
        Config config = new Config();
        config.setProperty("hazelcast.jmx", "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        // create and populate a distributed map
        IMap<Integer, Integer> map = hz.getMap("trial");
        for (int i = 0; i < 10; i++) {
            map.put(i, i * 3);
        }

        // connect to the JMX Service
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

        // See all Beans available
        Set<ObjectName> names = new TreeSet<ObjectName>(mbsc.queryNames(null, null));
        for (ObjectName name : names) {
            System.out.println("\tBean name: " + name);
        }

        // Bean name for the map
        ObjectName mapMBeanName = new ObjectName("com.hazelcast:instance=" + hz.getName() + ",type=IMap,name=trial");

        // usage of Getter methods
        System.out.println("\nTotal entries on map " + mbsc.getAttribute(mapMBeanName, "name") + ": "
                + mbsc.getAttribute(mapMBeanName, "localOwnedEntryCount"));

        // usage of invoking methods

        // get all map entries
        String[] params = {""};
        String[] signatures = {"java.lang.String"};
        String mapValues = (String) mbsc.invoke(mapMBeanName, "values", params, signatures);
        System.out.print("\nValues in the map: ");
        System.out.println(mapValues);

        // clear Map
        System.out.println("\nClearing the map...");
        mbsc.invoke(mapMBeanName, "clear", null, null);

        System.out.println("\nTotal entries on map " + mbsc.getAttribute(mapMBeanName, "name") + ": "
                + mbsc.getAttribute(mapMBeanName, "localOwnedEntryCount"));
    }
}
