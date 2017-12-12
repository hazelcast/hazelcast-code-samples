package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

/**
 * <p><u>{@code V1Flight}, version 1 of the data model</u></p>
 * <p>The simplest way, let Java do it.
 * </p>
 * <p>Pros:</p>
 * <ul>
 * <li><p>No code, just "{@code implements Serializable}"</p></li>
 * <li><p>No code, no tests, nothing to go wrong</p></li>
 * <li><p>Java standard</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>Reflection is a bit slow</p></li>
 * <li><p>Object contains a lot of Java meta-data, so slightly larger</p></li>
 * </ul>
 * <p><B>Summary:</B> Not optimal, ideal when this doesn't matter.</p>
 */
@EqualsAndHashCode(callSuper = false)
public class V1Flight extends AbstractFlight implements Serializable {

    /**
     * <p>Generated</p>
     */
    private static final long serialVersionUID = 6336918236919307393L;
}
