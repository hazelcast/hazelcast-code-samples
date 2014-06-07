import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapStreamSerializer implements StreamSerializer<HashMap> {

    @Override
    public int getTypeId() {
        return MySerializationConstants.HASHMAP_TYPE;
    }

    @Override
    public HashMap read(final ObjectDataInput in) throws IOException {
        int size = in.readInt();
        HashMap m = new HashMap(size);
        for (int k = 0; k < size; k++) {
            Object key = in.readObject();
            Object value = in.readObject();
            m.put(key, value);
        }
        return m;
    }

    @Override
    public void write(final ObjectDataOutput out, final HashMap m) throws IOException {
        out.writeInt(m.size());

        Set<Map.Entry> entrySet = m.entrySet();
        for (Map.Entry entry : entrySet) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    @Override
    public void destroy() {
    }
}