package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.Serializable;
import java.time.LocalDate;

import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>Basic representation of a flight from somewhere to somewhere else.
 * </p>
 * <ul>
 * <li><p><b>{@code code}</b> - the flight code, <i>HAZ123</i> etc.
 * </p>
 * <li><p><b>{@code date}</b> - date of departure.
 * </p>
 * <li><p><b>{@code rows}</b> - the rows of seats on the plane, and who is in each.
 * </p>
 * </ul>
 */
@Getter
@Setter
@SuppressWarnings("serial")
public abstract class AbstractFlight implements Serializable {

    private String code;
    private LocalDate date;
    private Person[][] rows;

    /**
     * <p>Pretty print the plane seating over multiple lines.
     * </p>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("%n");

        sb.append("Flight [%n Code=").append(code).append(",%n Date=").append(date).append("%n");

        if (rows == null) {
            sb.append(" Rows=null%n");
        } else {
            for (int i = 0 ; i < rows.length ; i++) {
                sb.append(" Row ").append(String.format("%2d [ ", i));
                Person[] row = rows[i];

                for (int j = 0 ; j < row.length ; j++) {
                    if (j == 3) {
                        // Aisle
                        sb.append("   ");
                    }

                    sb.append(" ").append(Constants.ALPHABET.charAt(j)).append("- ");
                    if (row[j] == null) {
                        sb.append("......");
                    } else {
                        String occupier = row[j].getName().toUpperCase() + "      ";
                        sb.append(occupier.subSequence(0, 6));
                    }
                }
                sb.append(" ]%n");
            }
        }

        sb.append("]%n");
        return String.format(sb.toString());
    }

}
