!/bin/bash
set -e
SCRIPTDIR=`dirname $0`
CLUSTER_NAME=Docker
if  [ -n $1 ]
then
  CLUSTER_NAME=$1
fi

JOBNAME=temp_monitor_`date +%H%M%S`
echo $JOBNAME > $SCRIPTDIR/../job/${CLUSTER_NAME}_job.txt
 clc -c $CLUSTER_NAME job submit \
    --verbose \
    --class=hazelcast.platform.labs.machineshop.TemperatureMonitorPipeline \
    --name=$JOBNAME \
    $SCRIPTDIR/../monitoring-pipeline/target/monitoring-pipeline-1.0-SNAPSHOT.jar

# Submit the solution instead
#  clc -c $CLUSTER_NAME job submit \
#      --verbose \
#      --class=hazelcast.platform.labs.machineshop.solutions.TemperatureMonitorPipelineSolution \
#      --name=$JOBNAME \
#      $SCRIPTDIR/../monitoring-pipeline/target/monitoring-pipeline-1.0-SNAPSHOT.jar
