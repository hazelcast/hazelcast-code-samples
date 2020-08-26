package example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CallableExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
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
        clientConfig.getUserCodeDeploymentConfig().addClass(ProcessAll.class).setEnabled(true);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IQueue<Product> productsQueue = client.getQueue("productsQueue");

        productsQueue.offer(new Product("foo", 10));
        productsQueue.offer(new Product("bar", 30));

        IExecutorService executorService = client.getExecutorService("distExecutor");

        Future future = executorService.submitToKeyOwner(new ProcessAll(), "productsQueue");

        //prints 40
        System.out.println(future.get());


    }
}
