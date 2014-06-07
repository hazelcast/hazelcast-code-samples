import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.util.LinkedList;

public class LinkedListStreamSerializer implements StreamSerializer<LinkedList> {

    @Override
    public int getTypeId() {
        return MySerializationConstants.LINKEDLIST_TYPE;
    }

    @Override
    public void write(ObjectDataOutput out, LinkedList l) throws IOException {
        out.writeInt(l.size());
        for (Object o : l) {
            out.writeObject(o);
        }
    }

    @Override
    public LinkedList read(ObjectDataInput in) throws IOException {
        LinkedList l = new LinkedList();
        int size = in.readInt();
        for (int k = 0; k < size; k++) {
            l.add(in.readObject());
        }
        return l;
    }

    @Override
    public void destroy() {
    }
}