package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class RingbufferSample {

    public static void main(String[] args){
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Ringbuffer called "my-ringbuffer"
        final Ringbuffer<Integer> rb = hz.getRingbuffer("my-ringbuffer");
        // Start a separate Thread that prints out the elements of the Ringbuffer.
        new MyThread(rb).start();
        // On the main thread add elements to the Ringbuffer
        for(int k=0;k<100;k++){
            rb.add(k);
        }
    }

    private static class MyThread extends Thread {
        private final Ringbuffer<Integer> rb;

        public MyThread(Ringbuffer<Integer> rb) {
            this.rb = rb;
        }

        @Override
        public void run(){
            try {
                long seq = rb.tailSequence();
                for (; ; ) {
                    System.out.println(rb.readOne(seq));
                    seq++;
                }
            }catch (InterruptedException e){
            }
        }
    }
}