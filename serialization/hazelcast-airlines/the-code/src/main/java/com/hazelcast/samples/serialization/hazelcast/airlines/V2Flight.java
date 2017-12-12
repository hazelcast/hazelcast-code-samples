package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * <p><u>{@code V2Flight}, version 2 of the data model</u></p>
 * <p>We take control of serializable and deserialization of fields.
 * </p>
 * <p>Pros:</p>
 * <ul>
 * <li><p>Not much code, just input and output added</p></li>
 * <li><p>Java standard</p></li>
 * <li><p>Can optimise the input and output logic</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>Still some reflection</p></li>
 * <li><p>Have to write tests or suffer the consequences of risking it</p></li>
 * </ul>
 * <p><B>Summary:</B> Is the coding worth the bother ?</p>
 */
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class V2Flight extends AbstractFlight implements Externalizable {

    /**
     * <p>Simply write the fields out
     * </p>
     */
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(this.getCode());
        objectOutput.writeObject(this.getDate());
        objectOutput.writeObject(this.getRows());
        log.trace("Serialize {}", this.getClass().getSimpleName());
    }

    /**
     * <p>Read them back in again
     * </p>
     */
    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        this.setCode(objectInput.readUTF());
        this.setDate((LocalDate) objectInput.readObject());
        this.setRows((Person[][]) objectInput.readObject());
        log.trace("De-serialize {}", this.getClass().getSimpleName());
    }














    /* --------------------------------------------------------------------------
     *  Take 2... a more clever implementation, raising the risk of logic faults
     *   so thorough testing required
     * --------------------------------------------------------------------------
     */

    /**
     * <p>A slightly cleverer version. Only write out those rows which
     * are not empty. Depending how full the seats are this might work
     * out as a smaller serialized object.
     * </p>
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(this.getCode());
        objectOutput.writeObject(this.getDate());

        Person[][] rows = this.getRows();

        // Size of each row, assume all the same
        objectOutput.writeInt(rows[0].length);
        // How many rows in total
        objectOutput.writeInt(rows.length);

        // Handle each row
        for (int i=0 ; i < rows.length ; i++) {
            Person[] row = rows[i];

            boolean empty = com.hazelcast.samples.serialization.hazelcast.airlines.util.Helpers.emptyRow(row);
            log.trace("Row {} empty: {}", i, empty);

            // So the receiver knows what to expect
            objectOutput.writeBoolean(empty);
            if (!empty) {
                objectOutput.writeObject(row);
            }
        }
        log.trace("Serialize {}", this.getClass().getSimpleName());
    }
     */

    /**
     * <p>The counterpart logic to write
     * </p>
    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        this.setCode(objectInput.readUTF());
        this.setDate((LocalDate)objectInput.readObject());

        // Size of each row, all the same
        int j = objectInput.readInt();
        // How many rows in total
        int i = objectInput.readInt();

        Person[][] rows = new Person[i][j];
        this.setRows(rows);

        // Handle each row
        for (int k=0 ; k < i; k++) {
            boolean empty = objectInput.readBoolean();
            log.trace("Row {} empty: {}", k, empty);

            if (empty) {
                rows[k] = new Person[j];
            } else {
                rows[k] = (Person[]) objectInput.readObject();
            }
        }

        log.trace("De-serialize {}", this.getClass().getSimpleName());
    }
     */

}
