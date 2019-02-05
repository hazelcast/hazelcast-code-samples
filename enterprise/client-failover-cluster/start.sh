#!/bin/sh

# open the first cluster
# Example log:
# Members {size:2, ver:2} [
#        Member [127.0.0.1]:5701 - 3fef408e-f271-42c0-b38b-2f69af54d90b
#        Member [127.0.0.1]:5702 - 3636657e-5cb9-46cf-aff1-b0d8b2da87ef this
#]
mvn exec:java -Dexec.mainClass="sample.Cluster1" &
export pidOfFirstCluster=$!


# open the second cluster
# Example log:
# Members {size:2, ver:2} [
#        Member [127.0.0.1]:5703 - 3b6daefb-76c1-4ec6-b935-2869a2836cee
#        Member [127.0.0.1]:5704 - 660fbb0c-abb1-4a1a-bca3-a9b6650f354e this
#]
mvn exec:java -Dexec.mainClass="sample.Cluster2" &

# wait for clusters to form
sleep 20

# start a client that is configured programmatically to fail-over to the second cluster when disconnected
mvn exec:java -Dexec.mainClass="sample.ProgrammaticFailoverExample"  &

# start a client that is configured via xml to fail-over to the second cluster when disconnected
mvn exec:java -Dexec.mainClass="sample.DeclarativeFailoverExample"  &

# wait for clients to connect
sleep 20

# kill first cluster
kill -9 ${pidOfFirstCluster}

# clients will log the change
# Example log:
# INFO: hz.client_0 [cluster1] [3.12] HazelcastClient 3.12 (20190205 - 3af10e3) is CLIENT_CHANGED_CLUSTER
