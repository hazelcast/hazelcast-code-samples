package com.hazelcast.samples.serialization.hazelcast.airlines;

import lombok.EqualsAndHashCode;

/**
 * <p><u>{@code V5Flight}, version 5 of the data model</u></p>
 * <p>Use Esoteric Software's <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>
 * rather than write our own serializer.
 * </p>
 * <p>Serialization logic is in {@link V5FlightSerializer}.
 * </p>
 * <p>Pros:</p>
 * <ul>
 * <li><p>We don't write code, can't get it wrong</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>What if someone else gets it wrong</p></li>
 * </ul>
 * <p><B>Summary:</B> Fast, generic, what's not to like</p>
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = false)
public class V5Flight extends AbstractFlight {
}
