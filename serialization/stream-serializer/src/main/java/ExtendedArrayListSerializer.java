import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class ExtendedArrayListSerializer implements StreamSerializer<ExtendedArrayList> {

    @Override
    public void write(ObjectDataOutput out, ExtendedArrayList object) throws IOException {
        out.writeBoolean(object != null);
        if (object != null) {
            int size = object.size();
            out.writeInt(size);
            for (Object anObject : object) {
                out.writeObject(anObject);
            }
        }
    }

    @Override
    public ExtendedArrayList read(ObjectDataInput in) throws IOException {
        if (in.readBoolean()) {
            int size = in.readInt();
            ExtendedArrayList result = new ExtendedArrayList();
            for (int i = 0; i < size; i++) {
                result.add(i, (Car) in.readObject());
            }
            return result;
        }
        return null;
    }

    @Override
    public int getTypeId() {
        return MySerializationConstants.EXTENDED_ARRAYLIST_TYPE.getId();
    }

    @Override
    public void destroy() {
    }
}
