package example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class EntryProcessorExample {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig().addPortableFactory(1, new PortableFactory() {
            @Override
            public Portable create(int classId) {
                if (classId == 1) {
                    return new Product();
                }
                return null;
            }
        });
        //Note that we are not loading domain objects
        clientConfig.getUserCodeDeploymentConfig().addClass(AddQuantityEntryProcessor.class).setEnabled(true);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Integer, Product> map = client.getMap("products");

        map.put(1, new Product("foo", 10));
        map.put(2, new Product("bar", 30));

        map.executeOnEntries(new AddQuantityEntryProcessor("foo", 30));

        //prints example.Product{name='foo', quantity=40}
        System.out.println(map.get(1));
        //prints example.Product{name='bar', quantity=30}
        System.out.println(map.get(2));


    }
}
