import com.hazelcast.collection.ICollection;
import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ItemListenerMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ICollection<String> queue = hz.getQueue("queue");
        queue.addItemListener(new ItemListenerImpl<String>(), true);
        System.out.println("ItemListener started");
    }

    private static class ItemListenerImpl<E> implements ItemListener<E> {
        @Override
        public void itemAdded(ItemEvent<E> itemEvent) {
            System.out.println("Item added:" + itemEvent.getItem());
        }

        @Override
        public void itemRemoved(ItemEvent<E> itemEvent) {
            System.out.println("Item removed:" + itemEvent.getItem());
        }
    }
}
