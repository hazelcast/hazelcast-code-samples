package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.IOException;
import java.time.LocalDate;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * <p><u>{@code V4Flight}, version 4 of the data model</u></p>
 * <p>Identify the object with a code number rather than the
 * class name.
 * </p>
 * <p>Pros:</p>
 * <ul>
 * <li><p>Codes not text represent the object class in byte stream, smaller</p></li>
 * <li><p>No Java! Interoperable with .Net, C++, etc</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>A bit more complexity compared to {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}</p></li>
 * </ul>
 * <p><B>Summary:</B> Worth the extra compared {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}</p>
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class V4Flight extends AbstractFlight implements IdentifiedDataSerializable {

    /**
     * <p>Simply write the fields out
     * </p>
     * <p>This is the same as for {@link V3Flight#writeData()}.
     * </p>
     */
    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(this.getCode());
        objectDataOutput.writeObject(this.getDate());
        objectDataOutput.writeObject(this.getRows());
        log.trace("Serialize {}", this.getClass().getSimpleName());
    }

    /**
     * <p>Read them back in again
     * </p>
     * <p>This is the same as for {@link V3Flight#readData()}.
     * </p>
     */
    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.setCode(objectDataInput.readUTF());
        this.setDate((LocalDate) objectDataInput.readObject());
        this.setRows((Person[][]) objectDataInput.readObject());
        log.trace("De-serialize {}", this.getClass().getSimpleName());
    }


    /**
     * <p>Which class builds this object on the receiver
     * </p>
     */
    @Override
    public int getFactoryId() {
        return Constants.MY_DATASERIALIZABLE_FACTORY;
    }

    /**
     * <p>The code the factory uses to work out which kind of object to build.
     * </p>
     */
    @Override
    public int getId() {
        return Constants.V4FLIGHT_ID;
    }

}
