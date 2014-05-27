import com.hazelcast.core.*;
import com.hazelcast.mapreduce.*;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spi.NodeEngine;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by gluck on 27/05/2014.
 */
public class MapReduce {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance hz3 = Hazelcast.newHazelcastInstance();

        IMap<Integer, Integer> m1 = hz1.getMap("default");
        for (int i = 0; i < 100; i++) {
            m1.put(i, i);
        }

        JobTracker tracker = hz1.getJobTracker("default");
        KeyValueSource<Integer, Integer> kvs = KeyValueSource.fromMap(m1);
        KeyValueSource<Integer, Integer> wrapper = new MyMapKeyValueSourceAdapter<Integer, Integer>(kvs);
        Job<Integer, Integer> job = tracker.newJob(wrapper);
        ICompletableFuture<Map<String, List<Integer>>> future = job.mapper(new MyMapper()).submit();

        Map<String, List<Integer>> result = future.get();

        for (String s : result.keySet()) {
            System.out.println("String: " + s);
        }
    }

    public static class MyMapper
            implements Mapper<Integer, Integer, String, Integer> {

        @Override
        public void map(Integer key, Integer value, Context<String, Integer> collector) {
            collector.emit(String.valueOf(key), value);
        }
    }


    public static class MyMapKeyValueSourceAdapter<K, V>
            extends KeyValueSource<K, V>
            implements DataSerializable, PartitionIdAware {

        private volatile KeyValueSource<K, V> keyValueSource;
        private int openCount = 0;

        public MyMapKeyValueSourceAdapter() {
        }

        public MyMapKeyValueSourceAdapter(KeyValueSource<K, V> keyValueSource) {
            this.keyValueSource = keyValueSource;
        }

        @Override
        public boolean open(NodeEngine nodeEngine) {
            if (openCount < 2) {
                openCount++;
                return false;
            }
            return keyValueSource.open(nodeEngine);
        }

        @Override
        public boolean hasNext() {
            return keyValueSource.hasNext();
        }

        @Override
        public K key() {
            return keyValueSource.key();
        }

        @Override
        public Map.Entry<K, V> element() {
            return keyValueSource.element();
        }

        @Override
        public boolean reset() {
            return keyValueSource.reset();
        }

        public static <K1, V1> KeyValueSource<K1, V1> fromMap(IMap<K1, V1> map) {
            return KeyValueSource.fromMap(map);
        }

        public static <K1, V1> KeyValueSource<K1, V1> fromMultiMap(MultiMap<K1, V1> multiMap) {
            return KeyValueSource.fromMultiMap(multiMap);
        }

        public static <V1> KeyValueSource<String, V1> fromList(IList<V1> list) {
            return KeyValueSource.fromList(list);
        }

        public static <V1> KeyValueSource<String, V1> fromSet(ISet<V1> set) {
            return KeyValueSource.fromSet(set);
        }

        @Override
        public void close()
                throws IOException {
            keyValueSource.close();
        }

        @Override
        public void writeData(ObjectDataOutput out)
                throws IOException {
            out.writeObject(keyValueSource);
        }

        @Override
        public void readData(ObjectDataInput in)
                throws IOException {
            keyValueSource = in.readObject();
        }

        @Override
        public void setPartitionId(int partitionId) {
            if (keyValueSource instanceof PartitionIdAware) {
                ((PartitionIdAware) keyValueSource).setPartitionId(partitionId);
            }
        }
    }



}
