package com.hazelcast.ocp.command;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.ocp.entryprocessor.DistanceProcessor;
import com.hazelcast.ocp.entryprocessor.Position;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class DefaultMapService implements MapService {

    private static final String MAP_NAME = "MapService";
    private static final String GPS = "GpsService";

    @Autowired
    private HazelcastInstance client;

    @Override
    public int insert(int count) {
        IMap<String, String> map = client.getMap(MAP_NAME);

        IntStream.range(0, count).forEach(it -> {
            String key = RandomStringUtils.randomAlphanumeric(42);
            String value = RandomStringUtils.randomAlphabetic(42);
            map.put(key, value);
        });

        return map.size();
    }

    @Override
    public int stats() {
        return Stream.of(MAP_NAME, GPS)
                .mapToInt(it -> client.getMap(it).size())
                .sum();
    }

    @Override
    public void runAutoPilot() {
        AutoPilot autoPilot = AutoPilot
                .builder()
                .map(client.getMap(MAP_NAME))
                .poolSize(10)
                .readCount(10_000)
                .insertCount(10_000)
                .valueSize(1024)
                .build();

        autoPilot.start();
    }

    @Override
    public void clear() {
        Stream.of(MAP_NAME, GPS)
                .forEach(it -> client.getMap(it).clear());
    }

    @Override
    public int insertPositions(int keyCount) {
        final IMap<String, Position> map = client.getMap(GPS);
        IntStream.range(0, keyCount).forEach(it -> {
            String key = RandomStringUtils.randomAlphabetic(16);
            double longitude = Math.random() * Math.PI * 2;
            double latitude = Math.acos(Math.random() * 2 - 1);
            Position rand = new Position(latitude, longitude);
            map.put(key, rand);
        });

        return keyCount;
    }

    @Override
    public long processDistances(Position position) {
        DistanceProcessor processor = new DistanceProcessor(position);
        IMap<String, Position> map = client.getMap(GPS);
        Map<String, Object> processed = map.executeOnEntries(processor);

        return processed.values()
                .stream()
                .filter(Position.class::isInstance)
                .map(Position.class::cast)
                .filter(it -> !Double.isNaN(it.getDistance()))
                .count();
    }
}
