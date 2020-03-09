package com.hazelcast.ocp.entryprocessor;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;

import java.util.Map;

public class DistanceProcessor extends AbstractEntryProcessor<String, Position> {

    private final Position poi;

    public DistanceProcessor(Position center) {
        this.poi = center;
    }

    private double distance(Position p1, Position p2) {
        double theta = p1.getLongitude() - p2.getLongitude();
        double dist = Math.sin(deg2rad(p1.getLatitude())) * Math.sin(deg2rad(p2.getLatitude()))
                + Math.cos(deg2rad(p1.getLatitude())) * Math.cos(deg2rad(p2.getLatitude())) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public Object process(Map.Entry<String, Position> entry) {
        Position value = entry.getValue();
        final double distance = distance(poi, value);
        value.setDistance(new Distance(poi, distance));
        entry.setValue(value);

        return value;
    }

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Position> positions = hz.getMap("positions");
        Position poi = new Position(52.5200d, 13.4050d);

        positions.put("Paris", new Position(48.8566d, 2.3522d));
        positions.put("New York", new Position(40.7128d, 74.0059d));
        positions.put("London", new Position(51.5074d, 0.1278d));

        positions.executeOnEntries(new DistanceProcessor(poi));

        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            final double distance = entry.getValue().getDistance();
            System.out.println(entry.getKey() + " distance to POI is " + distance);
        }

        Hazelcast.shutdownAll();
    }
}
