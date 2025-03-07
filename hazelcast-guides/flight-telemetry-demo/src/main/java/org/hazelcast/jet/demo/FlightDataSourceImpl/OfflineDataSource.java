package org.hazelcast.jet.demo.FlightDataSourceImpl;

import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonArray;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.SourceBuilder.TimestampedSourceBuffer;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.logging.ILogger;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import org.hazelcast.jet.demo.Aircraft;
import org.hazelcast.jet.demo.IFlightDataSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;

public class OfflineDataSource implements IFlightDataSource {

    public static final String OFFLINE_DATA_MAP_SUFFIX = "_OfflineData";
    private final long pollIntervalMillis;

    // holds a list of known aircraft, with last seen
    private final Map<String, Long> aircraftLastSeenAt = new HashMap<>();
    private final IMap<String, HazelcastJsonValue> offlineDataMap;

    private final ILogger logger;

    private long initialTimeDifference = 0L;

    private long lastPollEpoch;
    private long currentEpoch;

    private final AtomicLong minEpoch  = new AtomicLong();

    private final String regionName;

    private OfflineDataSource(ILogger logger, String regionName, long pollIntervalMillis, long startEpoch, IMap<String, HazelcastJsonValue> offlineDataMap ) {

        this.logger = logger;
        this.pollIntervalMillis = pollIntervalMillis;
        this.regionName = regionName;
        this.offlineDataMap = offlineDataMap;
        minEpoch.set(startEpoch);
    }

    public void fillBuffer(TimestampedSourceBuffer<Aircraft> buffer) {

        currentEpoch = System.currentTimeMillis();

        if (lastPollEpoch == 0L) {
            lastPollEpoch = currentEpoch;
            initialTimeDifference = currentEpoch - minEpoch.get();
            logger.fine("The time difference between now and the minimum epoch loaded is [" + initialTimeDifference + "] where now is [" + currentEpoch + "] and min epoch loaded is [ " + minEpoch.get() + "]");
        }

        // If the last poll was more than pollIntervalMillis ago then poll again
        if ((lastPollEpoch + pollIntervalMillis) < currentEpoch) {

            JsonObject response = pollForAircraft();

            lastPollEpoch = currentEpoch;

            // Compared to the FlightDataSource there is no need to filer by reg number (as the offline data
            // is already filtered), by last seen (as each line in the offline data is a new position) nor do we need
            // to calculate posTime from the timestamp in the API response however we do need to adjust based on the
            // time difference between the offline data and when the data was loaded
            if (response != null) {
                JsonArray aircraftList = response.get("ac").asArray();
                aircraftList.values().stream()
                        .map(IFlightDataSource::parseAircraft)
                        .filter(a -> !isNullOrEmpty(a.getr())) // there should be a reg number
                        .filter(a -> a.getpos_time() + initialTimeDifference > aircraftLastSeenAt.getOrDefault(a.getid(), 0L))
                        .forEach(a -> {
                            long positionTime = a.getpos_time() + initialTimeDifference;

                            // Let's update the aircraft timestamp fields so that it looks like LIVE data
                            a.setpos_time(positionTime);
                            a.setnow(a.getnow() + initialTimeDifference);

                            aircraftLastSeenAt.put(a.getid(), positionTime);
                            buffer.add(a, positionTime);
                        });

                logger.info("Polled " + aircraftList.size() + " aircraft, " + buffer.size() + " new positions.");
            } else {
                logger.info("Poll for aircraft failed.");
            }
        }
    }

    public JsonObject pollForAircraft() {

        logger.finest("[" + regionName + "] now > " + (lastPollEpoch - initialTimeDifference) + " AND now <= " + (currentEpoch - initialTimeDifference));
        Predicate predicate = Predicates.sql("now > " + (lastPollEpoch - initialTimeDifference) + " AND now < " + (currentEpoch - initialTimeDifference));

        Collection<HazelcastJsonValue> ac = this.offlineDataMap.values(predicate);

        JsonArray acJsonArray = new JsonArray();

        ac.forEach(e -> acJsonArray.add(Json.parse(e.toString())));

        JsonObject object = new JsonObject();
        object.add("ac", acJsonArray);

        return object;
    }

    public static StreamSource<Aircraft> getDataSource(String url, String regionName, long pollIntervalMillis, long startEpoch) {

        return SourceBuilder.timestampedStream("Flight Data Source",
                        ctx -> new OfflineDataSource(ctx.logger(), regionName, pollIntervalMillis, startEpoch, ctx.hazelcastInstance().getMap(regionName + OfflineDataSource.OFFLINE_DATA_MAP_SUFFIX)))
                .fillBufferFn(OfflineDataSource::fillBuffer)
                .build();
    }



}
