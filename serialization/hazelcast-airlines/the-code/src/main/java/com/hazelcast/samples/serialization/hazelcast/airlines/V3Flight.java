package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.IOException;
import java.time.LocalDate;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * <p><u>{@code V3Flight}, version 3 of the data model</u></p>
 * <p>We take control and optimize for Hazelcast
 * </p>
 * <p>Pros:</p>
 * <ul>
 * <li><p>Still not much code, just input and output</p></li>
 * <li><p>Hazelcast specific</p></li>
 * <li><p>Can optimise the input and output logic</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>Still some reflection</p></li>
 * <li><p>Have to write tests or suffer the consequences of risking it</p></li>
 * </ul>
 * <p><B>Summary:</B> Is it worth the change from {@code java.io.Serializable} ?</p>
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class V3Flight extends AbstractFlight implements DataSerializable {

    /**
     * <p>Simply write the fields out
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
     */
    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.setCode(objectDataInput.readUTF());
        this.setDate((LocalDate) objectDataInput.readObject());
        this.setRows((Person[][]) objectDataInput.readObject());
        log.trace("De-serialize {}", this.getClass().getSimpleName());
    }














    /* --------------------------------------------------------------------------
     *  Take 2... a more clever implementation, raising the risk of logic faults
     *   so thorough testing required
     * --------------------------------------------------------------------------
     */

    /**
     * <p>A slightly cleverer version. Only write out those rows which
     * are not empty.Depending how full the seats are this might work
     * out as a smaller serialized object.
     * </p>
     *
    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(this.getCode());
        objectDataOutput.writeObject(this.getDate());

        Person[][] rows = this.getRows();

        // Size of each row, assume all the same
        objectDataOutput.writeInt(rows[0].length);
        // How many rows in total
        objectDataOutput.writeInt(rows.length);

        // Handle each row
        for (int i=0 ; i < rows.length ; i++) {
            Person[] row = rows[i];

            boolean empty = com.hazelcast.samples.serialization.hazelcast.airlines.util.Helpers.emptyRow(row);
            log.trace("Row {} empty: {}", i, empty);

            // So the receiver knows what to expect
            objectDataOutput.writeBoolean(empty);
            if (!empty) {
                    //String[] names = new String[row.length];
                    //for(int l=0; l<row.length; l++) {
                    //    names[l] = row[l] == null ? "" : row[l].getName();
                    //}
                    //objectDataOutput.writeUTFArray(names);
                objectDataOutput.writeObject(row);
            }
        }
        log.trace("Serialize {}", this.getClass().getSimpleName());
    }
    */


    /**
     * <p>The counterpart logic to write
     * </p>
     *
    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.setCode(objectDataInput.readUTF());
        this.setDate((LocalDate)objectDataInput.readObject());

        // Size of each row, all the same
        int j = objectDataInput.readInt();
        // How many rows in total
        int i = objectDataInput.readInt();

        Person[][] rows = new Person[i][j];
        this.setRows(rows);

        // Handle each row
        for (int k=0 ; k < i; k++) {
            boolean empty = objectDataInput.readBoolean();
            log.trace("Row {} empty: {}", k, empty);

            if (empty) {
                rows[k] = new Person[j];
            } else {
                    //String[] names = (String[]) objectDataInput.readUTFArray();
                    //rows[k] = new Person[names.length];
                    //for (int l=0 ; l<names.length; l++) {
                    //    if (!names[l].equals("")) {
                    //        Person person = new Person(names[l]);
                    //        rows[k][l] = person;
                    //    }
                    //}
                rows[k] = (Person[]) objectDataInput.readObject();
            }
        }

        log.trace("De-serialize {}", this.getClass().getSimpleName());
    }
    */

}
