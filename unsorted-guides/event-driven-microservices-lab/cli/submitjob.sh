#!/bin/bash
set -e
mkdir -p /project/job
JOBNAME=temp_monitor_`date +%H%M%S`
hz-cli submit \
    -v \
    -c=hazelcast.platform.labs.machineshop.TemperatureMonitorPipeline \
    -t=dev@hz   \
    -n=$JOBNAME \
    /project/pipelines/target/pipelines-1.0-SNAPSHOT.jar
echo $JOBNAME > /project/job/jobname.txt
