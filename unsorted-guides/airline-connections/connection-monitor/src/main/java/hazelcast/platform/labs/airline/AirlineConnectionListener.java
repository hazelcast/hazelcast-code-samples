package hazelcast.platform.labs.airline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.query.Predicates;

/*
 * The Hazelcast connection is configured by providing the name of a cluster that has been
 * imported into clc with the "clc config import" or "clc viridian import-config" command.
 *
 * NOTE: The configuration must have been imported using clc version 5.3.3 or later. If the
 *       configuration was imported with an earlier version of clc it will need to be re-imported.
 *
 * See ConnectionHelper for more details on how configuring connections works.
 *
 * This program creates a listener on the "live_connections" map for connections where the
 * connection time is less than the minimum connection time (connect_status = 'AT RISK')
 *
 */
public class AirlineConnectionListener {
    public static void main(String []args){
        HazelcastInstance hz;
        String clusterName = args.length > 0 ? args[0] : null;

        hz = ConnectionHelper.connect(clusterName);

        Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));

        hz.<String,HazelcastJsonValue>getMap("live_connections")
                .addEntryListener(new Listener(), Predicates.sql("connection_status = 'AT RISK'"), true);

    }

    private static void printConnectionStatus(ObjectMapper mapper, String json){
        try {
            ObjectNode connection = (ObjectNode) mapper.readTree(json);
            String arrivingFlight = connection.get("arriving_flight").asText();
            String arrivalGate = connection.get("arrival_gate").asText();
            String arrivalTime = connection.get("arrival_time").asText();
            arrivalTime = arrivalTime.substring(arrivalTime.length() - 5);
            String departingFlight = connection.get("departing_flight").asText();
            String departureGate = connection.get("departure_gate").asText();
            String departureTime = connection.get("departure_time").asText();
            departureTime = departureTime.substring(departureTime.length() - 4);
            int connectionMinutes = connection.get("connection_minutes").asInt();

            System.out.println(arrivingFlight + " ARRIVING " + arrivalTime + " AT GATE " + arrivalGate + " CONNECTING TO "
                    + departingFlight + " DEPARTING " + departureTime + " FROM GATE " + departureGate + " ("
                    + connectionMinutes + " minutes)");

        } catch (JsonProcessingException e) {
            System.out.println("Error printing connection status: " + json);
        }
    }


    private static class Listener implements EntryAddedListener<String,HazelcastJsonValue>, EntryUpdatedListener<String,HazelcastJsonValue> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void entryAdded(EntryEvent<String, HazelcastJsonValue> entryEvent) {
            printConnectionStatus(mapper, entryEvent.getValue().getValue());
        }

        @Override
        public void entryUpdated(EntryEvent<String, HazelcastJsonValue> entryEvent) {
            printConnectionStatus(mapper, entryEvent.getValue().getValue());
        }
    }
}
