package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import java.io.Serializable;

public class ExecutorServiceSample {
    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        // Start a Second Embedded Hazelcast Cluster Member
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        // Get the Distributed Executor Service
        IExecutorService ex = hz1.getExecutorService("my-distributed-executor");
        // Submit the MessagePrinter Runnable to a random Hazelcast Cluster Member
        ex.submit(new MessagePrinter("message to any node"));
        // Get the first Hazelcast Cluster Member
        Member firstMember = hz1.getCluster().getMembers().iterator().next();
        // Submit the MessagePrinter Runnable to the first Hazelcast Cluster Member
        ex.executeOnMember(new MessagePrinter("message to very first member of the cluster"), firstMember);
        // Submit the MessagePrinter Runnable to all Hazelcast Cluster Members
        ex.executeOnAllMembers(new MessagePrinter("message to all members in the cluster"));
        // Submit the MessagePrinter Runnable to the Hazelcast Cluster Member owning the key called "key"
        ex.executeOnKeyOwner(new MessagePrinter("message to the member that owns the following key"), "key");
        // Shutdown this Hazelcast Cluster Member
        hz1.shutdown();
        // Shutdown this Hazelcast Cluster Member
        hz2.shutdown();
    }

    static class MessagePrinter implements Runnable, Serializable {
        final String message;

        MessagePrinter(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println(message);
        }
    }
}
