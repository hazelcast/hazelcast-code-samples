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

DROP JOB IF EXISTS update_connections;

DROP MAPPING IF EXISTS departures;

DROP MAPPING  IF EXISTS arrivals;

DROP VIEW  IF EXISTS arrivals_ordered;

DROP VIEW  IF EXISTS departures_ordered;

DROP MAPPING  IF EXISTS "connections";

DROP MAPPING  IF EXISTS minimum_connection_times;

DELETE FROM local_mct;

DROP MAPPING  IF EXISTS local_mct;

DELETE FROM local_connections;

DROP MAPPING  IF EXISTS local_connections;

DROP MAPPING  IF EXISTS live_connections;
