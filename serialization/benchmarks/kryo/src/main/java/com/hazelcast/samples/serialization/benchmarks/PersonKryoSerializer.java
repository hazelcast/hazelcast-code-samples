package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

/**
 * <p>
 * A {@link com.hazelcast.nio.serialization.StreamSerializer} for Kryo
 * in Java
 * </p>
 */
public class PersonKryoSerializer implements StreamSerializer<PersonKryo> {

    // Kryo instance is not threadsafe, but expensive, so that is why it is placed in a ThreadLocal.
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(PassportKryo.class);
            kryo.register(PersonKryo.class);
            return kryo;
        }
    };

    @Override
    public int getTypeId() {
        return MyConstants.PERSON_KRYO_SERIALIZER;
    }

    @Override
    public void write(ObjectDataOutput out, PersonKryo object) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();

        Output output = new Output((OutputStream) out);
        kryo.writeObject(output, object);
        output.flush();
    }

    @Override
    public PersonKryo read(ObjectDataInput in) throws IOException {
        Input input = new Input((InputStream) in);
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        return kryo.readObject(input, PersonKryo.class);
    }

}
