#!/bin/bash
set -x

TAG_KEY=$1
TAG_VALUE=$2


sed -i -e "s/TAG_KEY/${TAG_KEY}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/TAG_VALUE/${TAG_VALUE}/g" ${HOME}/hazelcast.yaml

nohup hz start -c ${HOME}/hazelcast.yaml >> ${HOME}/hazelcast.stdout.log 2>> ${HOME}/hazelcast.stderr.log &
sleep 5
