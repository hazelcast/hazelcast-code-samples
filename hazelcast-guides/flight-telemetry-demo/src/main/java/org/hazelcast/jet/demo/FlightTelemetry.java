package org.hazelcast.jet.demo;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import net.razorvine.pickle.Pickler;
import org.hazelcast.jet.demo.Aircraft.VerticalDirection;
import org.hazelcast.jet.demo.FlightDataSourceImpl.ADSBExchangeDataSource;
import org.hazelcast.jet.demo.FlightDataSourceImpl.OfflineDataSource;
import org.hazelcast.jet.demo.OfflineDataLoaderImpl.LocalFileSystemLoader;
import org.hazelcast.jet.demo.OfflineDataLoaderImpl.S3Loader;
import org.hazelcast.jet.demo.types.WakeTurbulanceCategory;
import org.hazelcast.jet.demo.util.HazelcastConnection.*;
import org.hazelcast.jet.demo.util.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static com.hazelcast.function.ComparatorEx.comparingInt;
import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.aggregate.AggregateOperations.*;
import static com.hazelcast.jet.pipeline.SinkBuilder.sinkBuilder;
import static java.util.Collections.emptySortedMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hazelcast.jet.demo.Aircraft.VerticalDirection.*;
import static org.hazelcast.jet.demo.types.WakeTurbulanceCategory.HEAVY;
import static org.hazelcast.jet.demo.util.HazelcastConnection.createHazelcastInstance;
import static org.hazelcast.jet.demo.util.Util.*;

/**
 * This demo reads an ADS-B telemetry stream from the [ADS-B Exchange](<a href="https://www.adsbexchange.com/">...</a>)
 * (see [ADS-B Exchange API](<a href="https://www.adsbexchange.com/data">...</a>)) which returns the
 * real-time positions of commercial aircraft flying within a defined radius of one or
 * more points around the globe.
 * <p>
 * The demo passes this flight telemetry data stream through a stream processing
 * pipeline created using the Hazelcast stream API. The pipeline consists of a series
 * of steps which filter, enrich and split the stream enabling some actions to be run
 * in parallel. The steps include multiple examples of how to use the stream API to
 * run custom code such as to perform geofencing during enrichment which adds the
 * local airport information to the data as well as examples of window functions
 * which are used to aggregate data over a sliding time window whilst running complex
 * custom algorithms which add co2 emissions and noise calculation (based on aircraft
 * type and vertical direction).
 * <p>
 * After all those calculations have been made the stream writes the results to
 * two maps (one for take-off and one for landing), the aircraft positions with co2
 * and noise calculations are sent to a Graphite sink for storage these are rendered
 * by a Grafana dashboard.
 * <p>
 * This demo can run in both online and offline mode. The online mode will retrieve
 * realtime flight positions from the ADS-B exchange API (please see the README for
 * instructions on how to sign-up for an ADS-B Exchange API key). The offline mode
 * uses around 30 minutes of flight data captured during September 2022 which
 * ADS-B Exchange have kindly allowed us to distribute for your convenience.
 * <p>
 * In summary, the demo will calculate following metrics in real-time
 * - Filter out planes outside defined airport / metropolitan areas
 * - Sliding over last 1 minute to detect, whether the plane is ascending, descending or staying in the same level
 * - Based on the plane type and phase of the flight provides information about maximum noise levels nearby to the airport and estimated C02 emissions for a region
 * <p>
 * The result of these are sent to three sinks (take off map, landing map and Graphite)
 * finally the data in these sinks are rendered in Grafana.
 * <p>
 * The DAG used to model Flight Telemetry calculations can be seen below:
 * The following DAG (displayed as ascii) illustrates the pipeline and executions paths
 * the same DAG can be seen in the application log on startup or viewed as an SVG
 * image in Hazelcast management center (the diagram in Management center is interactive
 * allowing you to drill into and monitor each job stage)
 * <pre>
 * <pre>
 * <p>
 *
 *                                                  ┌──────────────────┐
 *                                                  │Flight Data Source│
 *                                                  └─────────┬────────┘
 *                                                            │
 *                                                            v
 *                                           ┌─────────────────────────────────┐
 *                                           │Filter Aircraft  in Low Altitudes│
 *                                           └────────────────┬────────────────┘
 *                                                            │
 *                                                            v
 *                                                  ┌───────────────────┐
 *                                                  │Assign Airport Info│
 *                                                  └─────────┬─────────┘
 *                                                            │
 *                                                            v
 *                                          ┌───────────────────────────────────┐
 *                                          │Calculate Linear Trend of Altitudes│
 *                                          └─────────────────┬─────────────────┘
 *                                                            │
 *                                                            v
 *                                               ┌─────────────────────────┐
 *                                               │Assign Vertical Direction│
 *                                               └────┬────┬──┬───┬───┬────┘
 *                                                    │    │  │   │   │
 *                        ┌───────────────────────────┘    │  │   │   └──────────────────────────┐
 *                        │                                │  │   └─────────┐                    │
 *                        │                                │  └─────────┐   │                    │
 *                        v                                v            │   │                    │
 *             ┌────────────────────┐          ┌──────────────────────┐ │   │                    │
 *             │Enrich with C02 Info│          │Enrich with Noise Info│ │   │                    │
 *             └──┬─────────────────┘          └───────────┬──────────┘ │   │                    │
 *                │                                        │            │   │                    │
 *                │                          ┌─────────────┘            │   │                    │
 *                │                          │          ┌───────────────┘   │                    │
 *                v                          v          │                   v                    v
 * ┌───────────────────────┐ ┌─────────────────────────┐│ ┌───────────────────────────┐ ┌──────────────────────────┐
 * │Calculate Avg C02 Level│ │Calculate Max Noise Level││ │Filter Descending Aircraft │ │Filter Ascending Aircraft │
 * └──────────────┬────────┘ └────────────┬────────────┘│ └─────────────┬─────────────┘ └─────────┬────────────────┘
 *                │                       │             │               │                         │
 *                │  ┌────────────────────┘             │               │                         │
 *                │  │  ┌───────────────────────────────┘               │                         │
 *                │  │  │                                               │                         │
 *                │  │  │                                               │                         │
 *                v  v  v                                               v                         v
 *           ┌─────────────┐                               ┌──────────────────────┐     ┌────────────────────────┐
 *           │Graphite Sink│                               │IMap Sink (landingMap)│     │IMap Sink (takingOffMap)│
 *           └─────────────┘                               └──────────────────────┘     └────────────────────────┘
 * </pre>
 */
public class FlightTelemetry {

    private static final String CO2_EMISSION_KEY_SUFFIX = "_C02_EMISSION";
    private static final String AVG_NOISE_KEY_SUFFIX = "_AVG_NOISE";

    private static final int FLIGHT_TELEMETRY_SINK_PORT;
    private static final String TAKE_OFF_MAP = "takeOffMap";
    private static final String LANDING_MAP = "landingMap";
    private static final String FLIGHT_TELEMETRY_SINK_HOST;
    private static final String FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY;
    private static final String FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST;
    private static final String FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_URI;
    private static final String FLIGHT_TELEMETRY_USE_OFFLINE_DATA;
    private static final String FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE;
    private static final String FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE_PATH;
    private static final String FLIGHT_TELEMETRY_OFFLINE_DATA_SINK_PATH;
    private static final String FLIGHT_TELEMETRY_WRITE_TO_FILE;
    private static final String FLIGHT_TELEMETRY_HZ_INSTANCE_MODE;

    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_URL;
    private static final String FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME;

    // Use getHighBandwidthConfig to pull all global traffic, this results ina  single job and higher bandwidth usage
    private static final Map<String, Map<String, String>> configData = FlightDataSourceConfig.getLowBandwidthConfig();

    static {
        System.setProperty("hazelcast.logging.type", "log4j");

        // Config for ADSB exchange API
        FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY = getConfigurationParameter("FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY", "YOUR_API_KEY_HERE");
        FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST = getConfigurationParameter("FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST", "YOUR_API_HOST_HERE");
        FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_URI = getConfigurationParameter("FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_URI", "https://adsbexchange-com1.p.rapidapi.com/v2/lat/%.6f/lon/%.6f/dist/%d/");

        // General pipeline config
        FLIGHT_TELEMETRY_SINK_PORT = Integer.parseInt(getConfigurationParameter("FLIGHT_TELEMETRY_SINK_PORT", "2004"));
        FLIGHT_TELEMETRY_SINK_HOST = getConfigurationParameter("FLIGHT_TELEMETRY_SINK_HOST", "127.0.0.1");

        // Offline data source config
        FLIGHT_TELEMETRY_USE_OFFLINE_DATA = getConfigurationParameter("FLIGHT_TELEMETRY_USE_OFFLINE_DATA");
        FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE = getConfigurationParameter("FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE", "LocalFileSystem");
        FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE_PATH = getConfigurationParameter("FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE_PATH","flighttelemetry-offline-data");

        FLIGHT_TELEMETRY_OFFLINE_DATA_SINK_PATH = getConfigurationParameter("FLIGHT_TELEMETRY_OFFLINE_DATA_SINK_PATH","flighttelemetry-offline-data-out");

        FLIGHT_TELEMETRY_WRITE_TO_FILE = getConfigurationParameter("FLIGHT_TELEMETRY_WRITE_TO_FILE");
        FLIGHT_TELEMETRY_HZ_INSTANCE_MODE = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_INSTANCE_MODE", "embedded");

        // Config for viridian
        FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE", "client.keystore");
        FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD");
        FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE", "client.truststore");
        FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD");
        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN");
        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_URL = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_URL", "https://api.viridian.hazelcast.com");
        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME = getConfigurationParameter("FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME");
    }

    public static void main(String[] args) {
        boolean useOfflineDataSource = Boolean.parseBoolean(FLIGHT_TELEMETRY_USE_OFFLINE_DATA);
        boolean writeTelemetryToFile = Boolean.parseBoolean(FLIGHT_TELEMETRY_WRITE_TO_FILE);
        HazelcastInstance hzInstance = null;
        IOfflineDataLoader dataLoader = null;

        // If we are not using offline data check the required values have been passed
        if (!useOfflineDataSource) {
            if (
                    FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY.equals("YOUR_API_KEY_HERE")
            ) {
                System.err.println("\n" +
                        "ADSB_EXCHANGE_API_KEY must be set in one of the following :\n" +
                        "\tenvironment variable ADSB_EXCHANGE_API_KEY\n" +
                        "\tsystem property ADSB_EXCHANGE_API_KEY\n" +
                        "\tor provide it in FlightDataSource.java\n" +
                        "\n" +
                        "Using historical flight data to simulate real-time stream!");

                useOfflineDataSource = true;
            } else {
                System.out.println("Using ADSB Exchange API Key : " + FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY);
            }

            if (
                    FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST.equals("YOUR_API_HOST_HERE")
            ) {
                System.err.println("\n" +
                        "ADSB_EXCHANGE_API_HOST must be set in one of the following :\n" +
                        "\tenvironment variable ADSB_EXCHANGE_API_HOST\n" +
                        "\tsystem property ADSB_EXCHANGE_API_HOST\n" +
                        "\tor provide it in FlightDataSource.java\n" +
                        "\n" +
                        "Using historical flight data to simulate real-time stream!");

                useOfflineDataSource = true;
            } else {
                System.out.println("Using ADSB Exchange API Host : " + FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST);
            }
        }

        if (writeTelemetryToFile && !useOfflineDataSource) {
            System.out.println("Flight telemetry will be written to a file for future playback.");
        } else {
            if (writeTelemetryToFile) {
                System.out.println("WARNING: Both use offline data and write telemetry data to file are turned on however "
                        + "this is not allowed, writing telemetry to file will be automatically turned off.");
            }
            // Do not write offline data over itself
            writeTelemetryToFile = false;
        }

        if (useOfflineDataSource){
            if(FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE.equalsIgnoreCase(IOfflineDataLoader.DataSource.S3.toString())){
                dataLoader = new S3Loader();
            } else if(
                    FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE.equalsIgnoreCase(IOfflineDataLoader.DataSource.LOCAL_FILE_SYSTEM.toString())){
                dataLoader = new LocalFileSystemLoader();
            } else {
                System.out.println("ERROR: Unrecognised offline data loader please check FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE.");
                System.exit(1);
            }
        }

        try {
            if (FLIGHT_TELEMETRY_HZ_INSTANCE_MODE.equalsIgnoreCase(InstanceMode.VIRIDIAN.toString())) {
                hzInstance = createHazelcastInstance(
                        InstanceMode.VIRIDIAN.toString(), FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE,
                        FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD,
                        FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE,
                        FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD,
                        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME,
                        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN,
                        FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_URL
                );
            } else {
                hzInstance = createHazelcastInstance(FLIGHT_TELEMETRY_HZ_INSTANCE_MODE);
            }

        } catch (URISyntaxException | InstantiationException e) {
            System.out.println("ERROR: Could not create or connect to the Hazelcast instance (instance mode was set to [" + FLIGHT_TELEMETRY_HZ_INSTANCE_MODE +
                    "] the error reported was [" + e.getMessage() + "].");
            System.exit(1);
        }

        try {
            boolean finalUseOfflineDataSource = useOfflineDataSource;
            boolean finalWriteTelemetryToFile = writeTelemetryToFile;
            HazelcastInstance finalHzInstance = hzInstance;

            // Clear landing and takeoff maps
            hzInstance.getMap(TAKE_OFF_MAP).clear();
            hzInstance.getMap(LANDING_MAP).clear();

            addListener(hzInstance.getMap(TAKE_OFF_MAP), a -> System.out.println("New aircraft taking off: " + a));
            addListener(hzInstance.getMap(LANDING_MAP), a -> System.out.println("New aircraft landing " + a));

            IOfflineDataLoader finalDataLoader = dataLoader;
            configData.keySet().parallelStream()
                    .peek(e -> {
                        StreamSource<Aircraft> dataSource;

                        double radarLatitude = Double.parseDouble(configData.get(e).get("RADAR_LAT"));
                        double radarLongitude = Double.parseDouble(configData.get(e).get("RADAR_LON"));
                        int radarRadius = Integer.parseInt(configData.get(e).get("RADAR_RADIUS"));
                        int floor = Integer.parseInt(configData.get(e).get("FLOOR"));
                        int ceiling = Integer.parseInt(configData.get(e).get("CEILING"));

                        // Build the data source class based on on-line / off-line mode
                        if (finalUseOfflineDataSource) {

                            long startEpoch = finalDataLoader.loadData(
                                    e,
                                    finalHzInstance,
                                    FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE_PATH);
                            dataSource = OfflineDataSource.getDataSource("", e, 10000, startEpoch);

                        } else {
                            dataSource = ADSBExchangeDataSource.getDataSource(
                                    String.format(FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_URI, radarLatitude, radarLongitude, radarRadius),
                                    FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST,
                                    FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY,
                                    finalWriteTelemetryToFile,
                                    10000
                            );
                        }
                        System.out.println("Building job for [" + e + "]\n" +
                                "\tradar latitude : " + radarLatitude +
                                "\tradar longitude : " + radarLongitude +
                                "\tradar radius : " + radarRadius +
                                "\tfloor : " + floor +
                                "\tceiling : " + ceiling);

                        Pipeline pipeline = buildPipeline(e, dataSource, finalWriteTelemetryToFile, floor, ceiling);

                        JetService jetService = finalHzInstance.getJet();
                        Job job = jetService.newJob(pipeline, new JobConfig().setName("FlightTelemetry-" + e)
                                .setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE));

                        job.join();


                    }).findAny();
        } finally {
            Hazelcast.shutdownAll();
        }

        System.exit(0);
    }


    /**
     * Builds and returns the Pipeline which represents the actual computation.
     */
    private static Pipeline buildPipeline(String regionName, StreamSource<Aircraft> dataSource, boolean writeTelemetryToFile, int FLOOR, int CEILING) {
        Pipeline p = Pipeline.create();

        SlidingWindowDefinition slidingWindow = WindowDefinition.sliding(60_000, 30_000);

        StreamStage<Aircraft> ac;

        ac = p
                .readFrom(dataSource)
                .withNativeTimestamps(SECONDS.toMillis(15));

        if (writeTelemetryToFile) {
            ac.writeTo(Sinks.json(FLIGHT_TELEMETRY_OFFLINE_DATA_SINK_PATH + File.separator + Util.getFlightDataFilePrefix(regionName)));
        }

        // Filter aircraft whose altitude is less than 3000ft (plus floor), calculate linear trend of their altitudes
        // and assign vertical directions to them.
        StreamStage<KeyedWindowResult<String, Aircraft>> flights = ac.filter(a -> !a.isgnd() && a.getalt() > FLOOR &&
                        a.getalt() < CEILING).setName("Filter aircraft in low altitudes")
                .map(FlightTelemetry::assignAirport).setName("Assign airport")
                .window(slidingWindow)
                .groupingKey(Aircraft::getid)
                .aggregate(
                        allOf(toList(), linearTrend(Aircraft::getpos_time, Aircraft::getalt),
                                (events, coefficient) -> {
                                    Aircraft aircraft = events.get(events.size() - 1);
                                    aircraft.setverticaldirection(getVerticalDirection(coefficient));

                                    return aircraft;
                                })
                ).setName("Calculate linear trend of aircraft"); // (timestamp, aircraft_id, aircraft_with_assigned_trend)

        // Filter ascending flights
        StreamStage<KeyedWindowResult<String, Aircraft>> takingOffFlights = flights
                .filter(e -> e.getValue().getverticaldirection() == ASCENDING)
                .setName("Filter ascending aircraft");
        // Write ascending flights to an IMap
        takingOffFlights.writeTo(Sinks.map(TAKE_OFF_MAP)); // (aircraft_id, aircraft)

        //Filter descending flights
        StreamStage<KeyedWindowResult<String, Aircraft>> landingFlights = flights
                .filter(e -> e.getValue().getverticaldirection() == DESCENDING)
                .setName("Filter descending aircraft");
        // Write descending flights to an IMap
        landingFlights.writeTo(Sinks.map(LANDING_MAP)); // (aircraft_id, aircraft)

        // Enrich aircraft with the noise info and calculate max noise
        // in 60secs windows sliding by 30secs.
        StreamStage<KeyedWindowResult<String, Integer>> maxNoise = flights
                .map(e -> entry(e.getValue(), getNoise(e.getValue())))
                .setName("Enrich with noise info")// (aircraft, noise)
                .window(slidingWindow)
                .groupingKey(e -> e.getKey().getairport() + AVG_NOISE_KEY_SUFFIX)
                .aggregate(maxBy(comparingInt(Entry<Aircraft, Integer>::getValue)).andThen(Entry::getValue))
                .setName("Calculate max noise level");
        // (airport, max_noise)

        /* 
           For convenience, we just get the approximate loudest single aircraft in the window.

           If we wanted to calculate peak noise we could do something like calculate the combined noise:

                Sound pressure levels are expressed in decibels, which is a logarithmic scale. 
                Therefore, we cannot simply arithmetically add noise levels. For example, 35 dB
                plus 35 dB does not equal 70 dB. Part 5: Appendixes 5.3 To add two or more noise 
                levels, if the difference between the highest and next highest noise level is: 
                
                0–1 dB then add 3 dB to the higher level to give the total noise level 
                2–3 dB then add 2 dB to the higher level to give the total noise level 
                4–9 dB then add 1 dB to the higher level to give the total noise level 
                10 dB and over, then the noise level is unchanged (i.e. the higher level is the total level) 
                
                So, 35 dB plus 35 dB equals 38 dB. 
         * 
         */

        // Enrich aircraft with the C02 emission info and calculate total noise
        // in 60secs windows sliding by 30secs.
        StreamStage<KeyedWindowResult<String, Double>> co2Emission = flights
                .map(e -> entry(e.getValue(), getCO2Emission(e.getValue())))
                .setName("Enrich with CO2 info") // (aircraft, co2_emission)
                .window(slidingWindow)
                .groupingKey(entry -> entry.getKey().getairport() + CO2_EMISSION_KEY_SUFFIX)
                .aggregate(summingDouble(Entry::getValue))
                .setName("Calculate avg CO2 level");
        // (airport, total_co2)


        // Build Graphite sink
        Sink<KeyedWindowResult> graphiteSink = buildGraphiteSink(FLIGHT_TELEMETRY_SINK_HOST, FLIGHT_TELEMETRY_SINK_PORT);

        // Drain all results to the Graphite sink
        p.writeTo(graphiteSink, co2Emission, maxNoise, landingFlights, takingOffFlights)
                .setName("graphiteSink");

        return p;
    }

    /**
     * Sink implementation which forwards the items it receives to the Graphite.
     * Graphite's Pickle Protocol is used for communication between Jet and Graphite.
     *
     * @param host Graphite host
     * @param port Graphite port
     */
    private static Sink<KeyedWindowResult> buildGraphiteSink(String host, int port) {
        return sinkBuilder("graphite", instance ->
                new BufferedOutputStream(
                        new Socket(host, port).getOutputStream()))
                .<KeyedWindowResult>receiveFn((bos, entry) -> {
                    GraphiteMetric metric = new GraphiteMetric();
                    metric.from(entry);

                    Pickler pickler = new Pickler(false);
                    byte[] payload = pickler.dumps(metric.getAsList());
                    //PyString payload = cPickle.dumps(metric.getAsList(), 2);
                    byte[] header = ByteBuffer.allocate(4).putInt(payload.length).array();

                    bos.write(header);
                    bos.write(payload);
                })
                .flushFn(BufferedOutputStream::flush)
                .destroyFn(BufferedOutputStream::close)
                .build();
    }

    /**
     * Returns the average C02 emission on landing/take-off for the aircraft
     *
     * @param aircraft object
     * @return avg C02 for the aircraft
     */
    private static Double getCO2Emission(Aircraft aircraft) {
        // Note aircraft type in the property t (type refers to source of messages)
        return typeToLTOCycyleC02Emission.getOrDefault(aircraft.gett(), 0d);
    }

    /**
     * Returns the noise level at the current altitude of the aircraft
     *
     * @param aircraft object
     * @return noise level of the aircraft
     */
    private static Integer getNoise(Aircraft aircraft) {
        Long altitude = aircraft.getalt();
        SortedMap<Integer, Integer> lookupTable = getPhaseNoiseLookupTable(aircraft);
        if (lookupTable.isEmpty()) {
            return 0;
        }
        return lookupTable.tailMap(altitude.intValue()).values().iterator().next();
    }

    /**
     * Sets the airport field of the aircraft by looking at the coordinates of it
     *
     * @param aircraft object
     */
    private static Aircraft assignAirport(Aircraft aircraft) {
        if (aircraft.getalt() > 0 && !aircraft.isgnd()) {
            String airport = Airports.getAirport(aircraft.getlon(), aircraft.getlat());
            if (airport == null) {
                return null;
            }
            aircraft.setairport(airport);
        }
        return aircraft;
    }


    /**
     * Returns altitude to noise level lookup table for the aircraft based on its weight category
     *
     * @param aircraft object
     * @return SortedMap contains altitude to noise level mappings.
     */
    private static SortedMap<Integer, Integer> getPhaseNoiseLookupTable(Aircraft aircraft) {
        VerticalDirection verticalDirection = aircraft.getverticaldirection();
        WakeTurbulanceCategory wtc = aircraft.getwtc();
        if (ASCENDING.equals(verticalDirection)) {
            if (HEAVY.equals(wtc)) {
                return heavyWTCClimbingAltitudeToNoiseDb;
            } else {
                return mediumWTCClimbingAltitudeToNoiseDb;
            }
        } else if (DESCENDING.equals(verticalDirection)) {
            if (HEAVY.equals(wtc)) {
                return heavyWTCDescendAltitudeToNoiseDb;
            } else {
                return mediumWTCDescendAltitudeToNoiseDb;
            }
        }
        return emptySortedMap();
    }

    /**
     * Returns the vertical direction based on the linear trend coefficient of the altitude
     *
     * @param coefficient, a positive coefficient indicates an upwards trend (ascending), negative is downwards trend (descending)
     * @return VerticalDirection enum value
     */
    private static VerticalDirection getVerticalDirection(double coefficient) {
        if (coefficient > 0) {
            return ASCENDING;
        } else if (coefficient == 0) {
            return CRUISE;
        } else {
            return DESCENDING;
        }
    }

    /**
     * Attaches a listener to {@link IMap} which passes added items to the specified consumer
     *
     * @param map      map instance which the listener will be added
     * @param consumer aircraft consumer that the added items will be passed on.
     */
    private static void addListener(IMap<Long, Aircraft> map, Consumer<Aircraft> consumer) {
        map.addEntryListener((EntryAddedListener<Long, Aircraft>) event ->
                consumer.accept(event.getValue()), true);
    }

    /**
     * A data transfer object for Graphite
     */
    private static class GraphiteMetric {
        String metricName;//PyString metricName;
        Integer timestamp;//PyInteger timestamp;
        Float metricValue;//PyFloat metricValue;

        private GraphiteMetric() {
        }

        private void fromAirCraftEntry(KeyedWindowResult<String, Aircraft> aircraftEntry) {
            Aircraft aircraft = aircraftEntry.getValue();
            metricName = replaceWhiteSpace(aircraft.getairport()) + "." + aircraft.getverticaldirection(); //new PyString(replaceWhiteSpace(aircraft.getairport()) + "." + aircraft.getverticaldirection());
            timestamp = getEpochSecond(aircraft.getpos_time());//new PyInteger(getEpochSecond(aircraft.getpos_time()));
            metricValue = 1f; //new PyFloat(1);
        }

        private void fromMaxNoiseEntry(KeyedWindowResult<String, Integer> entry) {
            metricName = replaceWhiteSpace(entry.getKey()); //new PyString(replaceWhiteSpace(entry.getKey()));
            timestamp = getEpochSecond(entry.end()); //new PyInteger(getEpochSecond(entry.end()));
            metricValue = entry.getValue().floatValue(); //new PyFloat(entry.getValue());
        }

        private void fromTotalC02Entry(KeyedWindowResult<String, Double> entry) {
            metricName = replaceWhiteSpace(entry.getKey()); //new PyString(replaceWhiteSpace(entry.getKey()));
            timestamp = getEpochSecond(entry.end()); //new PyInteger(getEpochSecond(entry.end()));
            metricValue = entry.getValue().floatValue(); //new PyFloat(entry.getValue());
        }

        void from(KeyedWindowResult entry) {
            // We need to check which type of entry we are processing (we tagged the noise and co2 map keys with a suffix
            // so that we can easily work this out from the key name)
            if (entry.getKey() instanceof String &&
                    !(
                            entry.getKey().toString().endsWith(CO2_EMISSION_KEY_SUFFIX) ||
                                    entry.getKey().toString().endsWith(AVG_NOISE_KEY_SUFFIX)
                    )
            ) {
                fromAirCraftEntry(entry);
            } else {
                // If the key ends with the CO2 suffix, process as CO2 metric
                if (entry.getKey().toString().endsWith(CO2_EMISSION_KEY_SUFFIX)) {
                    fromTotalC02Entry(entry);
                } else {
                    // This must be a noise entry, process as noise metric
                    fromMaxNoiseEntry(entry);
                }
            }
        }

        List<Object> getAsList() {
            List<Object> list = new ArrayList<>();
            Object[] tupleValue = {timestamp, metricValue};
            Object[] metric = {metricName, tupleValue};

            list.add(metric);
            return list;
        }

        private int getEpochSecond(long millis) {
            return (int) Instant.ofEpochMilli(millis).getEpochSecond();
        }

        private String replaceWhiteSpace(String string) {
            return string.replace(" ", "_");
        }
    }

}
