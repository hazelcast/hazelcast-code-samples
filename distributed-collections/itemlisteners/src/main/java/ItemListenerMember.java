import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

public class ItemListenerMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ICollection<String> queue = hz.getQueue("queue");
        queue.addItemListener(new ItemListenerImpl<String>(), true);
        System.out.println("ItemListener started");
    }

    private static class ItemListenerImpl<E> implements ItemListener<E> {
        public void itemAdded(ItemEvent<E> itemEvent) {
            System.out.println("Item added:" + itemEvent.getItem());
        }

        public void itemRemoved(ItemEvent<E> itemEvent) {
            System.out.println("Item removed:" + itemEvent.getItem());
        }
    }
}
