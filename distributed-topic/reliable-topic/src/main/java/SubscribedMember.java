import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.ReliableMessageListener;

public class SubscribedMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ITopic<Long> topic = hz.getReliableTopic("sometopic");
        topic.addMessageListener(new ExampleReliableMessageListener());
    }

    private static class ExampleReliableMessageListener implements ReliableMessageListener<Long> {
        private long lastSequence;

        public long getLastSequence() {
            return lastSequence;
        }

        public void onMessage(Message<Long> m) {
            System.out.println("Received: " + m.getMessageObject());
        }

        @Override
        public long retrieveInitialSequence() {
            // The initial sequence to start reading from.
            return 1;
        }

        @Override
        public void storeSequence(long l) {
            // The sequence to store somewhere so that it can be retrieved upon restart.
            lastSequence = l;
            System.out.println("Stored sequence: " + l);
        }

        @Override
        public boolean isLossTolerant() {
            System.out.println("isLossTolerant called");
            // If true, the listener will not be removed upon an exception.
            return false;
        }

        @Override
        public boolean isTerminal(Throwable throwable) {
            System.out.println("isTerminal called");
            return false;
        }

        @Override
        public void onCancel() {
            System.out.println("onCancel called. The listener is being removed.");
        }
    }
}
