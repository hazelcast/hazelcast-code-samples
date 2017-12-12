package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>A serializer for {@link V5Flight} objects.
 * </p>
 * <p>Use Esoteric Software's <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>
 * which takes a byte stream.</p>
 * <p>This code could easily be made more generic. Although it deals with {@link V5Flight}
 * it does not mention the fields etc
 * </p>
 */
@Slf4j
public class V5FlightSerializer implements StreamSerializer<V5Flight> {

    /**
     * <p>"<i>Register</i>" the class so Kryo can figure out how to efficently
     * serialize it.
     * </p>
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(V5Flight.class);
            return kryo;
            }
    };


    /**
     * <p>Serialization : Hand Kryo the output stream and the object and leave it to do
     * it.
     * </p>
     */
    @Override
    public void write(ObjectDataOutput objectDataOutput, V5Flight v5Flight) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Output output = new Output((OutputStream) objectDataOutput);
        kryo.writeObject(output, v5Flight);
        output.flush();
        log.trace("Serialize {}", v5Flight.getClass().getSimpleName());
    }


    /**
     * <p>Deserialization : Give Kryo the input stream and ask it to read in an object
     * of the right type.
     * </p>
     */
    @Override
    public V5Flight read(ObjectDataInput objectDataInput) throws IOException {
        InputStream inputStream = (InputStream) objectDataInput;
        Input input = new Input(inputStream);
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        log.trace("De-serialize {}", V5Flight.class.getSimpleName());
        return kryo.readObject(input, V5Flight.class);
    }


    /**
     * <p>The id of the object being serialized/deserialized.
     * </p>
     */
    @Override
    public int getTypeId() {
        return Constants.V5FLIGHT_ID;
    }


    /**
     * <p>Not used, but called when the process shuts down.
     * </p>
     */
    @Override
    public void destroy() {
    }

}
