import com.hazelcast.core.*;

public class ListeningMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ReplicatedMap<String, String> map = hz.getReplicatedMap("somemap");
        map.addEntryListener(new MyEntryListener());
        System.out.println("EntryListener registered");
    }

    static class MyEntryListener implements EntryListener<String, String> {
        @Override
        public void entryAdded(EntryEvent<String, String> event) {
            System.out.println("entryAdded:" + event);
        }

        @Override
        public void entryRemoved(EntryEvent<String, String> event) {
            System.out.println("entryRemoved:" + event);
        }

        @Override
        public void entryUpdated(EntryEvent<String, String> event) {
            System.out.println("entryUpdated:" + event);
        }

        @Override
        public void entryEvicted(EntryEvent<String, String> event) {
            System.out.println("entryEvicted:" + event);
        }

        @Override
        public void mapEvicted(MapEvent event) {
            System.out.println("mapEvicted:" + event);

        }
        
        @Override
        public void mapCleared(MapEvent event) {
            System.out.println("mapCleared:" + event);
        }

    }
}
