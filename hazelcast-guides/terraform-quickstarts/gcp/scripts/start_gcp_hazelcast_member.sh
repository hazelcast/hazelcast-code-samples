#!/bin/bash
set -x

LABEL_KEY=$1
LABEL_VALUE=$2

sed -i -e "s/LABEL_KEY/${LABEL_KEY}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/LABEL_VALUE/${LABEL_VALUE}/g" ${HOME}/hazelcast.yaml

nohup hz start -c ${HOME}/hazelcast.yaml >> ${HOME}/hazelcast.stdout.log 2>> ${HOME}/hazelcast.stderr.log &
sleep 5
