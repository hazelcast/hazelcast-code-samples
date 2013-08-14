import com.hazelcast.core.*;
import com.hazelcast.util.executor.StripedExecutor;
import com.hazelcast.util.executor.StripedRunnable;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SubscribedMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ITopic<Date> topic = hz.getTopic("topic");
        topic.addMessageListener(new MessageListenerImpl("topic"));
        System.out.println("Subscribed");
    }

    private final static StripedExecutor executor = new StripedExecutor(
        Executors.newFixedThreadPool(10), 10
    );

    private static class MessageListenerImpl
            implements MessageListener<Date> {

        private final String topicName;

        public MessageListenerImpl(String topicName){
            this.topicName = topicName;
        }

        @Override
        public void onMessage(final Message<Date> m) {
            StripedRunnable task = new StripedRunnable() {
                @Override
                public int getKey() {
                    return topicName.hashCode();
                }

                @Override
                public void run() {
                    System.out.println("Received: " + m.getMessageObject());
                }
            };
            executor.execute(task);
        }
    }
}
