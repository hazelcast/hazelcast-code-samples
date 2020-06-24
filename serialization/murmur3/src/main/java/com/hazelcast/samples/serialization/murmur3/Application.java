package com.hazelcast.samples.serialization.murmur3;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import com.hazelcast.partition.Partition;
import com.hazelcast.spi.properties.ClusterProperty;

/**
 *  <p>A sample to predict and verify the <b>partition</b> that a <b>key</b>
 *  will be stored in, as this is deterministic.
 *  </p>
 *  <p>Which node hosts that partition is not deterministic, unless it's a
 *  one node cluster.
 *  </p>
 *  <p>For demonstration purposes, only integer keys are used. Other types
 *  such as strings could be added, but you'll end up duplicating the entire
 *  serialization code base.
 *  </p>
 */
public class Application {
    private static final String MAP_NAME = "numbers";
    private static final int PARTITION_COUNT = Integer.parseInt(ClusterProperty.PARTITION_COUNT.getDefaultValue());

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        // Turn off logging!
        properties.put("hazelcast.logging.type", "none");
        properties.put("hazelcast.partition.count", String.valueOf(PARTITION_COUNT));

        // Print partition count
        System.out.println("hazelcast.partition.count==" + properties.getProperty("hazelcast.partition.count"));

        Config config = new Config();
        config.setProperties(properties);

        // Create a 3 node cluster
        for (int i = 0 ; i < 3; i++) {
            config.setInstanceName("node" + i);
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            int size = hazelcastInstance.getCluster().getMembers().size();
            if (size == (i + 1)) {
                System.err.printf("Node '%s' in cluster, size now %d%n", hazelcastInstance.getName(), size);
            } else {
                System.err.println("************************************************");
                System.err.printf("Node '%s' not connected, turn on logging and try again%n", hazelcastInstance.getName());
                System.err.println("************************************************");
            }
        }

        HazelcastInstance anyInstance = Hazelcast.getAllHazelcastInstances().iterator().next();
        IExecutorService executorService = anyInstance.getExecutorService("default");
        MyCallable myCallable = new MyCallable();
        IMap iMap = anyInstance.getMap(MAP_NAME);

        // Predict and validate the placement of the first 25 integer keys.
        for (int i = 0 ; i < 25; i++) {
            byte[] value = new byte[i];
            iMap.set(i,  value);

            // Run our code to predict the partition for the key
            int[] prediction = predictedPartitionId(i, PARTITION_COUNT);
            int predictedPartitionId = prediction[0];
            int predictedHashCode = prediction[1];

            // Ask Hazelcast where the key actually is
            Future<String> node = executorService.submitToKeyOwner(myCallable, i);
            Partition partition = anyInstance.getPartitionService().getPartition(i);

            // See if they are the same!
            if (predictedPartitionId == partition.getPartitionId()) {
                System.out.printf("Key '%d', hashcode %d, is in partition %d, hosted currently by node %s%n",
                        i, predictedHashCode, predictedPartitionId, node.get());
            } else {
                System.err.println("************************************************");
                System.err.printf("ERROR: Key '%d'%n", i);
                System.err.printf("ERROR:   Predicted partition id '%d'%n", predictedPartitionId);
                System.err.printf("ERROR:   Actual partition id '%d'%n", partition.getPartitionId());
                System.err.println("************************************************");
            }
        }

        anyInstance.getCluster().shutdown();
    }

    /* A callable that returns the name of the node it is invoked on. We invoke
     * on the node that has a specific key, to find out which node that is.
     */
    @SuppressWarnings("serial")
    static class MyCallable implements Callable<String>, HazelcastInstanceAware, Serializable {

        private transient HazelcastInstance hazelcastInstance;

        @Override
        public String call() throws Exception {
            return this.hazelcastInstance.getName();
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance arg0) {
            this.hazelcastInstance = arg0;
        }
    }

    /* Calculate the hashcode and partition for a key
     */
    static int[] predictedPartitionId(int key, int partitionCount) {

        int hashCode = Math.abs(murmurHash3Int(key));

        int partitionId = hashCode % partitionCount;

        int[] result = new int[2];
        result[0] = partitionId;
        result[1] = hashCode;

        return result;
    }

    /* Murmur Hash 3, for integer input only. Java integers are 4 bytes.
     * This is the Hazelcast varient, for constants, and uses ">>>" in rotate not ">>".
     * See https://en.wikipedia.org/wiki/MurmurHash and
     * https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/java/com/hazelcast/internal/util/HashUtil.java
     */
    public static int murmurHash3Int(int i) {
        // Hazelcast seed
        int h = 0x01000193;

        // Hazelcast constants
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;

        // Byte order is Little Endian
        int k = Integer.reverseBytes(i);

        // Rotate 1
        k *= c1;
        k = (k << 15) | (k >>> 17);
        k *= c2;

        // Rotate 2
        h ^= k;
        h = (h << 13) | (h >>> 19);
        h = h * 5 + 0xe6546b64;

        // Finalize, uses 4 byte size and two Murmur3 constants
        h ^= 4;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;

        return h;
    }

}
