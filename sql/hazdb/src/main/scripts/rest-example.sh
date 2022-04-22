#!/bin/bash

# May need host machine IP for clustering
HOST_IP=`ifconfig | grep -w inet | grep -v 127.0.0.1 | cut -d" " -f2`
if [ "$HOST_IP" == "" ]
then
 HOST_IP=127.0.0.1
fi
if [ `echo $HOST_IP | wc -w` -ne 1 ]
then
 echo \$HOST_IP unclear:
 ifconfig | grep -w inet | grep -v 127.0.0.1
 exit 1
fi
HOST_IP=127.0.0.1

myRest() {
 URL="$*"
 URL=`echo $URL | sed 's/ /%20/g'`
 echo curl $URL
 curl "$URL"
 RC=$?
 echo ""
 echo RC=${RC}
 echo ""
}

myRest http://${HOST_IP}:5701/hazelcast/rest/cluster
myRest http://${HOST_IP}:5701/hazelcast/rest/instance
myRest http://${HOST_IP}:5701/hazelcast/health/cluster-size
echo \'node-state\' is used by Kubernetes livenessProbe
myRest http://${HOST_IP}:5701/hazelcast/health/node-state
echo \'ready\' is used by Kubernetes readinessProbe
myRest http://${HOST_IP}:5701/hazelcast/health/ready
myRest "http://${HOST_IP}:5701/hazelcast/rest/maps/stadium/BayArena"
