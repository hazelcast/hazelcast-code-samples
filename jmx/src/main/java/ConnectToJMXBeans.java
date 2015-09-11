import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.instance.GroupProperty;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

public class ConnectToJMXBeans {

    private static HazelcastInstance hz;
    private static int port = 9999;
    private static String hostname;
    private static JMXServiceURL url;

    public static void main(String[] args) throws Exception {
        // Parameters for connecting to the JMX Service
        hostname = InetAddress.getLocalHost().getHostName();
        url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + port + "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi");

        // Starting a Hazelcast member
        Config config = new Config();
        config.setProperty(GroupProperty.ENABLE_JMX, "true");
        hz = Hazelcast.newHazelcastInstance(config);

        // Create and populate a distributed map
        IMap<Integer, Integer> map = hz.getMap("trial");
        for(int i=0; i<10; i++) {
            map.put(i, i*3);
        }

        // Connect to the JMX Service
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        // See all Beans available
        Set<ObjectName> names =
                new TreeSet<ObjectName>(mbsc.queryNames(null, null));
        for (ObjectName name : names) {
            System.out.println("\tBean Name = " + name);
        }

        // Bean name for the map
        final ObjectName mapMBeanName = new ObjectName("com.hazelcast:instance="+hz.getName()+",type=IMap,name=trial");

        // Usage of Getter methods
        System.out.println("\nTotal entries on map " + mbsc.getAttribute(mapMBeanName, "name") + " : "
                + mbsc.getAttribute(mapMBeanName, "localOwnedEntryCount"));

        // Usage of Invoking methods
            // Get all map entries
        String [] params = {""};
        String [] signatures = {"java.lang.String"};
        String mapValues = (String) mbsc.invoke(mapMBeanName, "values", params, signatures);
        System.out.print("\nValues in the map: ");
        System.out.println(mapValues);
            // Clear Map
        System.out.println("\nClearing the map...");
        mbsc.invoke(mapMBeanName, "clear", null, null);

        System.out.println("\nTotal entries on map " + mbsc.getAttribute(mapMBeanName, "name") + " : "
                + mbsc.getAttribute(mapMBeanName, "localOwnedEntryCount"));
    }
}