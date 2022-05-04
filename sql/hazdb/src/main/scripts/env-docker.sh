#!/bin/bash
# 
cd `dirname $0`
GROUP=hazdb
TARGET=`basename $0 | sed 's/^docker-//' | sed 's/\.sh$//'`
FIRST=`echo $TARGET | cut -d- -f1`
SECOND=`echo $TARGET | cut -d- -f2`
THIRD=`echo $TARGET | cut -d- -f3`

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

if [ "$FIRST" == "hazelcast" ] && [ "$SECOND" == "server" ]
then
 MODULE=${FIRST}-${SECOND}
 CONTAINER=${FIRST}-${SECOND}-${THIRD}
 JAVA_ARGS="-e JAVA_ARGS=-Dhazelcast.local.publicAddress=${HOST_IP}:${DOCKER_PORT_EXTERNAL}"
 JAVA_ARGS="$JAVA_ARGS -e HOST_IP=$HOST_IP"
fi

if [ "$FIRST" == "hazelcast" ] && [ "$SECOND" == "client" ]
then
 MODULE=${FIRST}-${SECOND}
 CONTAINER=${FIRST}-${SECOND}
 JAVA_ARGS="-e HOST_IP=$HOST_IP"
fi

if [ "$FIRST" == "hazelcast" ] && [ "$SECOND" == "management" ] && [ "$THIRD" == "center" ]
then
 MODULE=${FIRST}-${SECOND}-${THIRD}
 CONTAINER=${FIRST}-${SECOND}-${THIRD}
fi

# Internal/external port mapping
if [ "$DOCKER_PORT_INTERNAL" == "" ]
then
 PORT_MAPPING=""
else
 PORT_MAPPING="-p ${DOCKER_PORT_EXTERNAL}:${DOCKER_PORT_INTERNAL}"
fi

# So can rerun named container
docker container prune --force > /dev/null 2>&1

DOCKER_IMAGE=${GROUP}/${MODULE}
CMD="docker run ${JAVA_ARGS} ${PORT_MAPPING} --rm --name=${CONTAINER} ${DOCKER_IMAGE}"
echo $CMD

$CMD
RC=$?
echo RC=${RC}
exit ${RC}
