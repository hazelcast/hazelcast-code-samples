package com.hazelcast.samples.querying.testdata;

/**
 * <a href="https://en.wikipedia.org/wiki/The_Three_Stooges">The Three
 * Stooges</a>
 */
public class TestData {

    // People and dates of birth
    public static final Object[][] BIRTHS = new Object[][] { { "Curly", "Howard", "1903-10-22" },
            { "Larry", "Fine", "1902-10-05" }, { "Moe", "Howard", "1897-06-19" }, { "Shemp", "Howard", "1895-03-11" },
            { "Joe", "Besser", "1907-08-12" }, { "Joe", "DeRita", "1909-07-12" }, };

    // Deaths for some of the above
    public static final Object[][] DEATHS = new Object[][] { { "Curly", "1952-01-18" }, { "Larry", "1975-01-24" },
            { "Moe", "1975-05-04" }, { "Shemp", "1955-11-22" }, };

}
