package member;

import com.hazelcast.core.*;

public class DistributedTopicSample implements MessageListener<String> {
    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get a Topic called "my-distributed-topic"
        ITopic<String> topic = hz.getTopic("my-distributed-topic");
        // Add a Listener to the Topic
        topic.addMessageListener(new DistributedTopicSample());
        // Publish a message to the Topic
        topic.publish("Hello to distributed world");
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }

    @Override
    public void onMessage(Message<String> message) {
        System.out.println("Got message " + message.getMessageObject());
    }
}