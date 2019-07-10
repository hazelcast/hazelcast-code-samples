package com.hazelcast.samples.querying.domain;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

import java.time.temporal.ChronoUnit;

/**
 * <P>
 * A {@link com.hazelcast.query.extractor.ValueExtractor ValueExtractor} builds
 * a field that can be used in queries, but isn't part of the object
 * </P>
 */
public class LifeAgeValueExtractor extends ValueExtractor<LifeValue, Integer> {

    /**
     * <P>
     * Calculate age in years, an {@code int} not a {@code long}.
     * </P>
     *
     * @param lifeValue
     *            The original object
     * @param unused
     *            An arguments for the extractor
     * @param valueCollector
     *            To add the extracted value
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void extract(LifeValue lifeValue, Integer unused, ValueCollector valueCollector) {

        long age = ChronoUnit.YEARS.between(lifeValue.getDateOfBirth(), lifeValue.getDateOfDeath());

        valueCollector.addObject((int) age);
    }

}
