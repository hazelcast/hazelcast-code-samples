package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;
import java.io.Serializable;

public class DistributedExecutorServiceSample {
    // The counterpart for this class should be implemented on the cluster side with same factory-id and class-id
    static class MessagePrinter implements Portable, Runnable {
        public static final int FACTORY_ID = 1;
        public static final int CLASS_ID = 9;

        public String message;

        MessagePrinter(String message) {
            this.message = message;
        }

        @Override
        public int getFactoryId() {
            return FACTORY_ID;
        }

        @Override
        public int getClassId() {
            return CLASS_ID;
        }

        @Override
        public void writePortable(PortableWriter writer) throws IOException {
            writer.writeUTF("message", message);
        }

        @Override
        public void readPortable(PortableReader reader) throws IOException {
            message = reader.readUTF("message");
        }

        @Override
        public void run() {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get the Distributed Executor Service
        IExecutorService ex = hz.getExecutorService("my-distributed-executor");
        // Submit the MessagePrinter Runnable to a random Hazelcast Cluster Member
        ex.submit(new MessagePrinter("message to any node"));
        // Get the first Hazelcast Cluster Member
        Member firstMember = hz.getCluster().getMembers().iterator().next();
        // Submit the MessagePrinter Runnable to the first Hazelcast Cluster Member
        ex.executeOnMember(new MessagePrinter("message to very first member of the cluster"), firstMember);
        // Submit the MessagePrinter Runnable to all Hazelcast Cluster Members
        ex.executeOnAllMembers(new MessagePrinter("message to all members in the cluster"));
        // Submit the MessagePrinter Runnable to the Hazelcast Cluster Member owning the key called "key"
        ex.executeOnKeyOwner(new MessagePrinter("message to the member that owns the following key"), "key");
        // Shutdown this Hazelcast Cluster Member
        hz.shutdown();
    }
}