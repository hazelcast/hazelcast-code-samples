package org.hazelcast.jet.demo.OfflineDataLoaderImpl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.map.IMap;
import org.hazelcast.jet.demo.IOfflineDataLoader;
import org.hazelcast.jet.demo.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.hazelcast.jet.demo.FlightDataSourceImpl.OfflineDataSource.OFFLINE_DATA_MAP_SUFFIX;

public class LocalFileSystemLoader implements IOfflineDataLoader {

    public long loadData(String regionName, HazelcastInstance hzInstance, String path) {
        AtomicLong minEpoch = new AtomicLong();
        AtomicLong counter = new AtomicLong();
        long dataLoadStartEpoch = System.currentTimeMillis();

        IMap<String, HazelcastJsonValue> offlineDataMap = hzInstance.getMap(regionName + OFFLINE_DATA_MAP_SUFFIX);

        offlineDataMap.clear();

        System.out.println("Starting offline data load from local file system (loading to an embedded cluster is usually quick however if your cluster is remote it may take some time).");

        try (
                Stream<Path> entries = Files.walk(Paths.get(path + File.separator + Util.getFlightDataFilePrefix(regionName)))
                        .filter(Files::isRegularFile)
        ) {
            entries.forEach(file -> {
                        try {
                            // Skip hidden files
                            if (!Files.isHidden(file)) {
                                try (Stream<String> lines = Files.lines(file)) {
                                    lines.forEach(line -> {
                                        JsonObject object = Json.parse(line).asObject();

                                        long updateEpoch = object.get("now").asLong();

                                        // If the position epoch looks valid then and is lower than minEpoch then update the minEpoch
                                        if (updateEpoch > 0 && (updateEpoch < minEpoch.get()) || minEpoch.get() <= 0) {
                                            if (minEpoch.get() <= 0){
                                                minEpoch.set(updateEpoch);
                                            } else {
                                                minEpoch.accumulateAndGet(updateEpoch, Math::min);
                                            }
                                        }

                                        offlineDataMap.put(UUID.randomUUID().toString(), new HazelcastJsonValue(line));
                                        counter.getAndIncrement();

                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully loaded offline data. A total of [" + offlineDataMap.entrySet().size() + "] flight positions were loaded for [" + regionName + "] in " + (System.currentTimeMillis() - dataLoadStartEpoch) + "ms (min epoch is [" + minEpoch.get() + "])");

        return minEpoch.get();
    }
}
