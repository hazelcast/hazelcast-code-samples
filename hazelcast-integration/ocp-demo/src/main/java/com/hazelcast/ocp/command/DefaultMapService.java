package com.hazelcast.ocp.command;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class DefaultMapService implements MapService{

    private static final String MAP_NAME = "MapService";

    @Autowired
    private HazelcastInstance client;

    @Override
    public int insert(int count) {

        IMap<String, String> map = client.getMap(MAP_NAME);

        IntStream.range(0, count).forEach(it ->{
            String key = RandomStringUtils.randomAlphanumeric(42);
            String value = RandomStringUtils.randomAlphabetic(42);
            map.put(key, value);
        });

        return map.size();

    }

    @Override
    public int stats() {
        return client.getMap(MAP_NAME).size();
    }

    @Override
    public void runAutoPilot(){

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
        client.getMap(MAP_NAME).clear();
    }
}
