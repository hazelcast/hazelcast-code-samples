-- Run this script with the Hazelcast CLC.
-- The Hazelcast CLC can be downloaded from https://github.com/hazelcast/hazelcast-commandline-client/releases/tag/v5.3.2
-- 
-- Before running the script, you will need to login to viridian and import 
-- the connection configuration for your cluster.  Once you have imported the connection configuration
-- it will be stored locally and it will not be necessary to import it again for future sessions.
--
-- To login and import a configuration, use the following steps 
-- CLC> \viridian login --api-key=xxxxx --api-secret=yyyyyyyy
-- CLC> \viridian import-config mycluster
--
--
-- This script assume that a Kafka connection called "DemoKafkaConnection" and 
-- a Postgres connection called "DemoPostgresConnection" already exist.
--
-- Create mappings to particular topics/tables using those data connections.
--
CREATE OR REPLACE MAPPING "departures"
EXTERNAL NAME "demo.flights.departures" (
  event_time timestamp with time zone,
  "day" date,
  flight varchar,
  airport varchar,
  departure_gate varchar,
  departure_time timestamp
)
DATA CONNECTION "DemoKafkaConnection"
OPTIONS (
    'keyFormat' = 'varchar',
    'valueFormat' = 'json-flat'
);


CREATE OR REPLACE MAPPING "arrivals"
EXTERNAL NAME "demo.flights.arrivals" (
  event_time timestamp with time zone,
  "day" date,
  flight varchar,
  airport varchar,
  arrival_gate varchar,
  arrival_time timestamp 
)
DATA CONNECTION "DemoKafkaConnection"
OPTIONS (
    'keyFormat' = 'varchar',
    'valueFormat' = 'json-flat'
);

-- Now tell Hazelcast how the streaming data is ordered

CREATE OR REPLACE VIEW arrivals_ordered AS
SELECT * FROM TABLE (
  IMPOSE_ORDER(
     TABLE arrivals, 
     DESCRIPTOR(event_time),  
     INTERVAL '1' HOUR
  )
);

CREATE OR REPLACE VIEW departures_ordered AS
SELECT * FROM TABLE (
  IMPOSE_ORDER(
     TABLE departures, 
     DESCRIPTOR(event_time),  
     INTERVAL '1' HOUR
  )
);


-- Postgres Mappings
CREATE OR REPLACE MAPPING "connections"
EXTERNAL NAME "public"."connections" (
  arriving_flight varchar,
  departing_flight varchar
)
DATA CONNECTION "DemoPostgresConnection";

CREATE OR REPLACE MAPPING "minimum_connection_times"
EXTERNAL NAME "public"."minimum_connection_times" (
  airport varchar,
  arrival_terminal varchar,
  departure_terminal varchar,
  minutes integer
)
DATA CONNECTION "DemoPostgresConnection";


--  copy mct and connection data from Postgres into IMaps
CREATE OR REPLACE MAPPING local_mct(
  airport varchar,
  arrival_terminal varchar,
  departure_terminal varchar,
  minutes integer
)
Type IMap 
OPTIONS (
    'keyFormat' = 'varchar',
  'valueFormat' = 'json-flat'
);

DELETE FROM local_mct;
INSERT INTO local_mct(__key, airport, arrival_terminal, departure_terminal, minutes) 
SELECT airport||arrival_terminal||departure_terminal, airport, arrival_terminal, departure_terminal, minutes 
FROM minimum_connection_times;

CREATE OR REPLACE MAPPING local_connections(
  arriving_flight varchar,
  departing_flight varchar
)
Type IMap 
OPTIONS (
    'keyFormat' = 'varchar',
  'valueFormat' = 'json-flat'
);

DELETE FROM local_connections;
INSERT INTO local_connections(__key, arriving_flight, departing_flight) 
SELECT arriving_flight || departing_flight, arriving_flight, departing_flight FROM "connections";

-- finally, create the job that joins everything up and sinks it to 

CREATE OR REPLACE MAPPING live_connections(
  arriving_flight varchar,
  arrival_gate varchar,
  arrival_time timestamp,
  departing_flight varchar,
  departure_gate varchar,
  departure_time timestamp,
  connection_minutes integer,
  mct integer,
  connection_status varchar
)
Type IMap 
OPTIONS (
    'keyFormat' = 'varchar',
  'valueFormat' = 'json-flat'
);

DROP JOB IF EXISTS update_connections;

CREATE JOB update_connections 
AS
SINK INTO live_connections(
  __key, 
  arriving_flight,
  arrival_gate,
  arrival_time,
  departing_flight,
  departure_gate,
  departure_time,
  connection_minutes,
  mct,
  connection_status
) 
SELECT 
  C.arriving_flight || C.departing_flight,
  C.arriving_flight,
  A.arrival_gate, 
  A.arrival_time, 
  C.departing_flight, 
  D.departure_gate, 
  D.departure_time,
  CAST((EXTRACT(EPOCH FROM D.departure_time) - EXTRACT(EPOCH FROM A.arrival_time))/60 AS INTEGER) AS connection_minutes,
  M.minutes as mct, 
  CASE 
    WHEN CAST((EXTRACT(EPOCH FROM D.departure_time) - EXTRACT(EPOCH FROM A.arrival_time))/60 AS INTEGER) < M.minutes THEN 'AT RISK'
    ELSE 'OK'
  END as connection_status
FROM arrivals_ordered A 
INNER JOIN local_connections C 
  ON C.arriving_flight = A.flight 
INNER JOIN departures_ordered D
  ON D.event_time BETWEEN A.event_time - INTERVAL '10' SECONDS AND A.event_time + INTERVAL '10' SECONDS 
  AND D.flight = C.departing_flight 
INNER JOIN local_mct M
ON A.airport = M.airport
AND SUBSTRING(A.arrival_gate FROM 1 FOR 1) = M.arrival_terminal 
AND SUBSTRING(D.departure_gate FROM 1 FOR 1) = M.departure_terminal;
