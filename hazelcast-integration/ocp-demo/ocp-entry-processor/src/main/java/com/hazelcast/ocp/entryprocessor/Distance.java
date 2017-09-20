package com.hazelcast.ocp.entryprocessor;

import java.io.Serializable;

public class Distance implements Serializable {

    private Position reference;
    private double distance;

    public Distance(Position reference, double distance) {
        this.reference = reference;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}
