import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.util.executor.StripedExecutor;
import com.hazelcast.util.executor.StripedRunnable;

import java.util.Date;

public class SubscribedMember {

    private static final ILogger LOGGER = Logger.getLogger(SubscribedMember.class);
    private static final StripedExecutor EXECUTOR = new StripedExecutor(
            LOGGER, "listeners", null, 10, 10000
    );

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ITopic<Date> topic = hz.getTopic("topic");
        topic.addMessageListener(new MessageListenerImpl("topic"));
        System.out.println("Subscribed");
    }

    private static class MessageListenerImpl implements MessageListener<Date> {

        private final String topicName;

        MessageListenerImpl(String topicName) {
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
            EXECUTOR.execute(task);
        }
    }
}
