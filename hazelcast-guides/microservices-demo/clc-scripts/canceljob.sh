#!/bin/bash
set -e
SCRIPTDIR=`dirname $0`
CLUSTER_NAME=Docker
if  [ -n $1 ]
then
  CLUSTER_NAME=$1
fi

clc -c $CLUSTER_NAME job cancel `cat $SCRIPTDIR/../job/${CLUSTER_NAME}_job.txt`