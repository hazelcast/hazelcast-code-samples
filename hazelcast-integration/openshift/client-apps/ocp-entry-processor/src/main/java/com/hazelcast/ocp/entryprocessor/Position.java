package com.hazelcast.ocp.entryprocessor;

import java.io.Serializable;

public class Position implements Serializable {

    private final double latitude;
    private final double longitude;

    private Distance distance;

    public Position() {
        this(0d, 0d);
    }

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public double getDistance() {
        if (null == distance) {
            return Double.NaN;
        }
        return distance.getDistance();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
