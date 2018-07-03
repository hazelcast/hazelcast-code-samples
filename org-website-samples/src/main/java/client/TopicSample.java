package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class TopicSample implements MessageListener<String> {
    @Override
    public void onMessage(Message<String> message) {
        System.out.println("Got message " + message.getMessageObject());
    }

    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Topic called "my-distributed-topic"
        ITopic<String> topic = hz.getTopic("my-distributed-topic");
        // Add a Listener to the Topic
        topic.addMessageListener(new TopicSample());
        // Publish a message to the Topic
        topic.publish("Hello to distributed world");
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
