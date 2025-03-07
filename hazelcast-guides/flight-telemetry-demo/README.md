# ADS-B Flight Telemetry Stream Processing Demo

This demo reads an ADS-B telemetry stream from the [ADS-B Exchange](https://www.adsbexchange.com/)
(see [ADS-B Exchange API](https://www.adsbexchange.com/data)) which returns the 
real-time positions of commercial aircraft flying within a defined radius of one or 
more points around the globe.

The demo passes this flight telemetry data stream through a stream processing 
pipeline created using the Hazelcast stream API. The pipeline consists of a series 
of steps which filter, enrich and split the stream enabling some actions to be run 
in parallel. The steps include multiple examples of how to use the stream API to 
run custom code such as to perform geofencing during enrichment which adds the 
local airport information to the data as well as examples of window functions
which are used to aggregate data over a sliding time window whilst running complex
custom algorithms which add co2 emissions and noise calculation (based on aircraft
type and vertical direction).

After all those calculations have been made the stream writes the results to
two maps (one for take-off and one for landing), the aircraft positions with co2
and noise calculations are sent to a Graphite sink for storage these are rendered
by a Grafana dashboard.

This demo can run in both online and offline mode. The online mode will retrieve
realtime flight positions from the ADS-B exchange API (please see the Prerequisites
section for more details on running in online or offline data and for instructions
on how to sign-up for an ADS-B Exchange API key). The offline mode uses around 30
minutes of flight data captured during September 2022 which ADS-B Exchange have
kindly allowed us to distribute for your convenience.

In summary, the demo will calculate following metrics in real-time
- Filter out planes outside defined airport / metropolitan areas
- Sliding over last 1 minute to detect, whether the plane is ascending, descending or staying in the same level 
- Based on the plane type and phase of the flight provides information about maximum noise levels nearby to the airport and estimated C02 emissions for a region

The result of these are sent to three sinks (take off map, landing map and Graphite)
finally the data in these sinks are rendered in Grafana.

## Package Level Structure
At the highest level, the source code is organized into three packages

- com.hazelcast.jet.demo       The main class (`FlightTelemetry`) with the main method and the domain model classes resides in this package.
- com.hazelcast.jet.demo.types The enum classes reside in this package.
- com.hazelcast.jet.demo.util  The utility helper classes reside in this package.


# Prerequisites

- ADS-B Exchange API Key

To run this demo fully (with real-time data) you will need to sign up for an API Key for ADS-B Exchange, you can find more 
information on the [ADS-B Exchange](https://www.adsbexchange.com/data)) website. The API is currently hosted on RapidAPI.com 
and costs around $10 (correct as of October 2022) for 10,000 requests a month.

Please note we cannot share our API key, if you cannot sign up with ADS-B Exchange you can test the demo with a small amount 
of offline data (see environment variables below and set FLIGHT_TELEMETRY_USE_OFFLINE_DATA to true) which ADS-B Exchange 
have kindly allowed us to distribute for your convenience. 

Once you have tried the demo in offline mode why not sign up for your own API key and modify the configuration to work in 
an area / with airports near you (hint you will need to change the configData map in the FlightTelemetry class -  you could 
even have one point and a large enough radius to cover the entire globe if your internet connection is fast enough - you 
will also need to add any airports you wish to monitor to the same file as well as add the logic to the getAirport 
method).

- Docker with Docker Compose: [Installation guide](https://docs.docker.com/install/)

Docker must be running for this demo to work. 

This demo application will output it's results to a Graphite database for visualization with Grafana as well as launch
an instance of Management center (only if running in embedded mode - for viridian use the cloud Management Center) 
which allows you to interact with the cluster.

You can easily start a Graphite and Grafana instance and Management center instance in Docker using the provided `docker-compose.yml` and  `Makefile` script.

The java application and docker-compose file makes use of a number of environment variables:

(variables relating to flight data source connection)
*  FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY - This should contain your ADS-B Exchange API key
*  FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST - This should contain the hostname for the ADS-B exchange API
*  FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_URI - This is the URL including parameter placeholders for the query service i.e. https://adsbexchange-com1.p.rapidapi.com/v2/lat/%.6f/lon/%.6f/dist/%d/

(variables relating to sink connection)
*  FLIGHT_TELEMETRY_SINK_HOST - This should contain the hostname or IP address for the docker host running the Graphite/Grafana instance (usually 127.0.0.1 or localhost)

(variables relating to other demo settings)
*  FLIGHT_TELEMETRY_HZ_INSTANCE_MODE - (embedded/Viridian/bootstrap) If you want to run Hazelcast locally choose embedded otherwise to run on Viridian cloud set this to Viridian and provide the additional settings below

(variables relating to Hazelcast Viridian see above, also, please read the Viridian section for additional steps)
*  FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME - Your Viridian cluster name (see quick connection guide in your Viridian cloud console or sign-up at https://viridian.hazelcast.com/)
*  FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN - Your Viridian cluster discovery token (see quick connection guide in your Viridian cloud console)
*  FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD - Your Viridian cluster keystore password (see quick connection guide in your Viridian cloud console)
*  FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD - Your Viridian cluster truststore password (see quick connection guide in your Viridian cloud console)

(variables relating to other demo settings)
*  FLIGHT_TELEMETRY_WRITE_TO_FILE - (True/False) If you are using the API to poll live aircraft data this determines if the filtered data is written to a file sink (which can later be used as an off-line data source)

(variables relating to Management Center configuration)
*  FLIGHT_TELEMETRY_HZ_MEMBER_LIST - This should contain the hostname or IP address of the demo application and is used to allow management center to connect to the cluster (usually 127.0.0.1 or localhost)

(variable related to use of offline data)
*  FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE - This should be set to either S3 or Local_File_System and determines where data is loaded from
*  FLIGHT_TELEMETRY_USE_OFFLINE_DATA - (True/False) If set to true the demo application with connect to the ADS-B Exchange API (note this API has a quota if you exceed it you will see HTTP 429 errors)

Examples for these environment variables can be found in the script sampleEnvironment.sh to use this on Linux/Mac use
the source command (or '.') as follows:

  source ./sampleEnvironment.sh 
OR
  . ./sampleEnvironment.sh

# Launching dependencies

You use following command to run the Management Center, Graphite and Grafana Docker image:

```bash
$ make up
```

When you are done with the demo, to stop the containers, run the following command:
```bash
$ make down
```

In case you need to log into the Graphite/Grafana docker container shell, run the following command:
```bash
$ make shell
```

In case you need to view the Graphite/Grafana container log , run the following command:
```bash
$ make tail
```

# Building the Application

Here we cover the maven command (and for the Enterprise edition pom.xml changes) required to build the application, the 
correct steps will depend on if you are planning to run the open source edition of Hazelcast or the Enterprise edition.

If you are running the open source version simply run (without changing the pom.xml):

```bash
mvn clean package
```

If you have a license for the enterprise version you will need change the pom.xml file to use the enterprise edition 
dependencies. Open the pom.xml in your favourite text editor and search for "Select your Hazelcast dependency" you will 
see two sections one for the open source edition and another for Viridian or Enterprise, uncomment the "viridian / enterprise" 
section and comment out the two lines in the open source section as follows:

```
        <!-- Select your Hazelcast dependency -->
        <!-- open source -->
        <!--hazelcast.version>5.2-SNAPSHOT</hazelcast.version-->
        <!--hazelcast.jar>hazelcast</hazelcast.jar-->

        <!-- viridian or enterprise -->
        <hazelcast.version>5.1</hazelcast.version>
        <hazelcast.jar>hazelcast-enterprise</hazelcast.jar>
```

With the pom.xml modified build the application in the same way as for the open source version (remember you will still 
need to supply the license key as described in the "Running the Application" section below).

# Running the Application

After building the application, you can run the application using either maven or java command lines.

Here we cover the maven commands used to run the application, the correct command line will depend on if you are planning 
to run the open source edition of Hazelcast or the Enterprise edition. 

If you are running the open source version simply run (without changing the pom.xml or setting an environment variable):

```bash
mvn exec:java
```

If you have a license for the enterprise version you will need to provide it using one of the following methods:

To run enterprise edition directly from the command line using maven you can provide the license key in the pom.xml or by 
setting the MAVEN_OPTS environment variable e.g. :

Using MAVEN_OPTS:

```bash setting MAVEN_OPTS
export MAVEN_OPTS=export MAVEN_OPTS="-Dhazelcast.enterprise.license.key=<YOUR_KEY_GOES_HERE>"                
mvn exec:java
```

or by setting a system property in the pom.xml:

Open the pom.xml and search for '<YOUR_KEY_GOES_HERE>' replace the placeholder with your own license key then uncomment
the systemProperties section.

You can now start the before application normally using:

```bash
mvn exec:java
```

Navigate with your browser to `http://localhost` (or your docker host IP addresses if not localhost) to open up the Grafana 
application.

NOTE:
* To log into Grafana use `admin/admin` username/password pair. 
* You need to select `Flight Telemetry` dashboard to see the metrics that are emitted from the Flight Telemetry application.

To monitor the cluster or query the take-off or landing maps in Management Center, navigate to `http://localhost:8080` (or your docker host IP addresses).

If you wish to query the take-off or landing maps click on 'SQL Browser', then 'Connect Maps', choose 'landingMap' / 'takeOffMap'
or repeat for both. Once connected you can try a query such as this (which returns all aircraft broadcasting a squawk code greater than 7000
which is generally used to signify some kind of emergency see https://pilotinstitute.com/squawk-codes-list/):

```
SELECT
  *
FROM
  (
    SELECT
      r,
      squawk,
      airport,
      t as type,
      'landing' as direction
    FROM
      "landingMap"
    where
      alt > 0
      AND squawk <> ''
      AND squawk IS NOT NULL
      AND CAST(squawk as INTEGER) >= 7000
    union
    SELECT
      r,
      squawk,
      airport,
      t as type,
      'take-off' as Direction
    FROM
      "takeOffMap"
    where
      alt > 0
      AND squawk <> ''
      AND squawk IS NOT NULL
      AND CAST(squawk as INTEGER) >= 7000
  )
group by
  r,
  squawk,
  airport,
  type,
  direction
order by
  r;
```

# Running on Viridian

If you want to run on Viridian (see https://viridian.hazelcast.com/) you will need to complete the following steps

1. Ensure you have configured the Viridian related environment variables (described above)
2. Copy your client.keystore, client.pfx and client.truststore files to /src/main/resources
3. Set the instance mode environment variable to viridian (See sampleEnvironment.sh)

NOTE: The demo may still startup a Management Center instance however this will not be used, please instead use the
Management Center instance linked to your Viridian cluster.



# IMPORTANT

This demo includes offline data provided by [ADS-B Exchange](https://www.adsbexchange.com/). Please consider supporting 
ADS-B Exchange by a [donation](https://www.adsbexchange.com/donate/), by hosting a [feeder](https://www.adsbexchange.com/how-to-feed/) 
ar by signing up for their [ADS-B Exchange API](https://www.adsbexchange.com/data) to run this demo with real-time data .

Note: The ADS-B data stream can publish large volumes of data, we are polling it every 10 seconds by default so you might 
need a decent internet connection for demo to work properly. Otherwise, you might see some delay/glitches on the charts/output.

