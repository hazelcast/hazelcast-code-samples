package org.hazelcast.jet.demo.FlightDataSourceImpl;

import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonArray;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.json.JsonValue;
import com.hazelcast.internal.util.ExceptionUtil;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.SourceBuilder.TimestampedSourceBuffer;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.logging.ILogger;
import org.hazelcast.jet.demo.Aircraft;
import org.hazelcast.jet.demo.IFlightDataSource;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.hazelcast.internal.util.StringUtil.isNullOrEmpty;

/**
 * Polls the <a href="https://www.adsbexchange.com">ADS-B Exchange</a> HTTP API
 * for flight data. The API will be polled every {@code pollIntervalMillis}
 * milliseconds.
 * <p>
 * After a successful poll, this source filters out aircraft which are missing
 * registration number
 * and position timestamp. It will also record the latest position timestamp of
 * the aircraft so if
 * there are no update for an aircraft it will not be emitted from this source.
 */
public class ADSBExchangeDataSource implements IFlightDataSource {

    /**
     * See <a href="https://www.adsbexchange.com/data/">ADS-B Exchange</a> for how
     * to
     * obtain an API key.
     */
    protected String API_AUTHENTICATION_KEY = "YOUR_API_KEY_HERE";

    /**
     * The API host is provided by Rapid API when you subscribe to the ADBSExchange
     * API.
     */
    protected String API_HOST = "YOUR_API_HOST_HERE";

    protected boolean WRITE_FLIGHT_TELEMETRY_TO_FILE;

    private final URL url;
    private final long pollIntervalMillis;

    // holds a list of known aircraft, with last seen
    private final Map<String, Long> aircraftLastSeenAt = new HashMap<>();

    private final ILogger logger;

    private long lastPoll;


    private ADSBExchangeDataSource(ILogger logger, String url, String host, String apiKey, Boolean writeTelemetryToFile, long pollIntervalMillis ) {

        this.logger = logger;

        try {
            this.url = new URL(url);
            if (!host.isEmpty()){
                this.API_HOST = host;
            }

            if(!apiKey.isEmpty()){
                this.API_AUTHENTICATION_KEY = apiKey;
            }

            this.WRITE_FLIGHT_TELEMETRY_TO_FILE = writeTelemetryToFile;
        } catch (MalformedURLException e) {
            throw ExceptionUtil.rethrow(e);
        }
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public void fillBuffer(TimestampedSourceBuffer<Aircraft> buffer) throws IOException {

        long currentEpoch = System.currentTimeMillis();

        if (currentEpoch > (lastPoll + pollIntervalMillis)) {

            lastPoll = currentEpoch;

            JsonObject response = pollForAircraft();

            if (response != null) {
                // Note, the documentation says now is seconds since the epoch but its actually ms
                long currentResponseTimeEpoc = response.get("now").asLong();
                JsonArray aircraftList = response.get("ac").asArray();
                aircraftList.values().stream()
                        .map(IFlightDataSource::parseAircraft)
                        .filter(a -> !isNullOrEmpty(a.getr())) // there should be a reg number
                        // only add new positions to buffer
                        .filter(a -> a.getpos_time(currentResponseTimeEpoc) > aircraftLastSeenAt.getOrDefault(a.getid(), 0L))
                        .forEach(a -> {
                            // Set "now" on the object for convenience (e.g. persisting offline data)
                            a.setnow(currentResponseTimeEpoc);

                            // The response from getPosTime denotes when the aircraft position was last reported
                            // when this getter is passed an epoch (read from the now property in the ADSBExchange v2
                            // response which indicates when the response was generated) the last position time is
                            // calculated using the passed epoch and seen or seenPos which is how many milliseconds since
                            // the position was last reported
                            long positionTime = a.getpos_time(currentResponseTimeEpoc);

                            // update cache
                            aircraftLastSeenAt.put(a.getid(), positionTime);
                            buffer.add(a, positionTime);
                        });

                logger.info("Polled " + aircraftList.size() + " aircraft, " + buffer.size() + " new positions.");
            } else {
                logger.info("Poll for aircraft failed.");
            }
        }
    }

    public JsonObject pollForAircraft() throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        StringBuilder response = new StringBuilder();

            try {
                con.setRequestMethod("GET");
                con.addRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
                con.addRequestProperty("X-RapidAPI-Key", API_AUTHENTICATION_KEY);
                con.addRequestProperty("X-RapidAPI-Host", API_HOST);

                int responseCode = con.getResponseCode();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                if (responseCode != 200) {
                    logger.info("API returned error: " + responseCode + " " + response);
                    return null;
                }
            } finally {
                con.disconnect();
            }
        JsonValue value = Json.parse(response.toString());
        return value.asObject();
    }

    public static StreamSource<Aircraft> getDataSource(String url, String host, String apiKey, Boolean writeTelemetryToFile, long pollIntervalMillis) {

        return SourceBuilder.timestampedStream("Flight Data Source",
                ctx -> new ADSBExchangeDataSource(ctx.logger(), url, host, apiKey, writeTelemetryToFile, pollIntervalMillis ))
                .fillBufferFn(ADSBExchangeDataSource::fillBuffer)
                .build();

    }

}
