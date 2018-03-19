package com.hazelcast.samples.spi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * In order to demonstrate if queues allow higher priority
 * items to overtake, we need something with a recognisable
 * natural sequence. Days of the week will do fine.
 * <p>
 * Ordinal numbers come from
 * <a href="https://en.wikipedia.org/wiki/ISO_8601#Week_dates">ISO8601</a>
 */

@RequiredArgsConstructor
public enum Day {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    @Getter
    private final int ordinal;
}
