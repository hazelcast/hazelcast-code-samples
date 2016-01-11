import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PersonKryoSerializer implements StreamSerializer<Person> {

    // Kryo instance is not threadsafe, but expensive, so that is why it is placed in a ThreadLocal.
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(Person.class);
            return kryo;
        }
    };

    public int getTypeId() {
        return 2;
    }

    public void write(ObjectDataOutput objectDataOutput, Person product) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();

        Output output = new Output((OutputStream) objectDataOutput);
        kryo.writeObject(output, product);
        output.flush();
    }

    public Person read(ObjectDataInput objectDataInput) throws IOException {
        InputStream in = (InputStream) objectDataInput;
        Input input = new Input(in);
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        return kryo.readObject(input, Person.class);
    }

    public void destroy() {
    }
}
