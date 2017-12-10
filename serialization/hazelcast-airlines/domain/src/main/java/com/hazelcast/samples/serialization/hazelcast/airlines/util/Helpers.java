package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import java.util.Random;

import com.hazelcast.samples.serialization.hazelcast.airlines.Person;

/**
 * <p>Communal helper functions
 * </p>
 */
public class Helpers {

    /**
     * <p>Count how many seats are in used.
     * </p>
     *
     * @param rows Array of array of seats
     * @return How many seats are in use
     */
    public static int countOccupied(Person[][] rows) {
        int count = 0;

        if (rows != null) {
            for (int i = 0 ; i < rows.length ; i++) {
                Person[] row = rows[i];
                for (int j = 0 ; j < row.length ; j++) {
                    if (row[j] != null) {
                        count++;
                    }
                }
            }
        }

        return count;
    }


    /**
     * <p>"<i>Randomly</i>" inject data into the seats of the plane.
     * Use the same random number seed every time so not at all
     * random.
     * </p>
     *
     * @param rows Array of array of seats
     */
    public static void loadRows(Person[][] rows) {
        Random random = new Random(Long.MAX_VALUE);

        for (String name : Constants.PEOPLE) {

            boolean found = false;
            while (!found) {
                int row = random.nextInt(rows.length);
                int seat = random.nextInt(rows[row].length);
                if (rows[row][seat] == null) {
                    rows[row][seat] = new Person(name);
                    found = true;
                }
            }
        }

    }

    /**
     * <p>Is a row of seats empty
     * </p>
     *
     * @param row Array of {@code Person}
     * @return True is all are null
     */
    public static boolean emptyRow(Person[] row) {
        for (Person person : row) {
            if (person != null) {
                return false;
            }
        }
        return true;
    }
}
