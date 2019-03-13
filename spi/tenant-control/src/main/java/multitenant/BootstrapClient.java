package multitenant;

import apps.app1.AddPersonProcessor;
import apps.app1.Person;
import apps.app2.IllegalPersonProcessor;
import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class BootstrapClient {

    public static void main(String[] args) {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        ICache app1Cache = client.getCacheManager().getCache("apps.app1");

        app1Cache.put(1, new Person(1, "Evan"));
        System.out.println("app1 cache value for key 1 is " + app1Cache.get(1));
        app1Cache.invoke(2, new AddPersonProcessor(), 2, "Sammy");
        System.out.println("app1 cache value for key 2 is " + app1Cache.get(2));

        ICache app2Cache = client.getCacheManager().getCache("apps.app2");
        // app2 cache's tenant control will setup context classloader so it cannot
        // load class Person --> app2Cache.invoke will fail
        try {
            app2Cache.invoke(3, new IllegalPersonProcessor(), 3, "Bill");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // null value is expected
        System.out.println("app2 cache value for key 3 is " + app2Cache.get(3));

        client.shutdown();
    }
}
