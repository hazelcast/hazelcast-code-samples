#!/bin/bash
set -x

REGION=$1
TAG_KEY=$2
TAG_VALUE=$3
CONN_RETRIES=$4
IAM_ROLE=$5

sed -i -e "s/REGION/${REGION}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/TAG_KEY/${TAG_KEY}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/TAG_VALUE/${TAG_VALUE}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/CONN_RETRIES/${CONN_RETRIES}/g" ${HOME}/hazelcast.yaml
sed -i -e "s/IAM_ROLE/${IAM_ROLE}/g" ${HOME}/hazelcast.yaml

nohup hz start -c ${HOME}/hazelcast.yaml >> ${HOME}/hazelcast.stdout.log 2>> ${HOME}/hazelcast.stderr.log &
sleep 5


