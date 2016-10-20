import com.hazelcast.core.RingbufferStore;

public class TheRingbufferBinaryStore implements RingbufferStore<byte[]> {

    @Override
    public void store(long sequence, byte[] data) {
        System.out.println("Binary store");
    }

    @Override
    public void storeAll(long firstItemSequence, byte[][] items) {
        System.out.println("Binary store all");
    }

    @Override
    public byte[] load(long sequence) {
        System.out.println("Binary load");
        return null;
    }

    @Override
    public long getLargestSequence() {
        System.out.println("Binary get largest sequence");
        return -1;
    }
}
