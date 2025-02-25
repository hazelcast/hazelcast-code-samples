# Set ADSB exchange API key for telemetry demo and source details
export FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_KEY=
export FLIGHT_TELEMETRY_ADSB_EXCHANGE_API_HOST=adsbexchange-com1.p.rapidapi.com

# Configure sink endpoint details
export FLIGHT_TELEMETRY_SINK_HOST=127.0.0.1

# Set the following to one of embedded, Viridian or bootstrap
export FLIGHT_TELEMETRY_HZ_INSTANCE_MODE=

# If running on Hazelcast Viridian ()
export FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_CLUSTERNAME=
export FLIGHT_TELEMETRY_HZ_CLIENT_CLOUD_DISCOVERYTOKEN=
export FLIGHT_TELEMETRY_HZ_CLIENT_KEYSTORE_PASSWORD=
export FLIGHT_TELEMETRY_HZ_CLIENT_TRUSTSTORE_PASSWORD=

# Configure other demo mode setting e.g. use offline data or write real-time data to file for future offline use
export FLIGHT_TELEMETRY_WRITE_TO_FILE=false
export FLIGHT_TELEMETRY_USE_OFFLINE_DATA=true
export FLIGHT_TELEMETRY_OFFLINE_DATA_SOURCE=Local_File_System

# Configure the member list (this is injected into the Management Center configuration)
export FLIGHT_TELEMETRY_HZ_MEMBER_LIST=127.0.0.1
