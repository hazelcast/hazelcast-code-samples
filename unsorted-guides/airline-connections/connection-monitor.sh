#!/bin/sh
#
# INSTRUCTIONS
# 
# Build the connection monitor using maven
#   cd connection-monitor
#   mvn clean package 
#   cd ..
#
# Set Up the Connection
#    copy "sample.env" to ".env" 
#    in the Viridian dashboard, click on  Java / Advanced Setup
#    download the keystore file and unzup it (anywhere) 
#    in ".env" set VIRIDIAN_SECRETS_DIR to the unzipped keystore directory 
#    copy the other values from the Advance Setup screen into .env
#
# Run the connection manager without arguments.  This will retrieve all of the connections
#    ./connection_monitor.sh 
# 
# Run the connection monitor with a departing and arriving flight to monitor connections
#   ./connection_monitor.sh  ABC123  DEF456
# 
#
set -e
SCRIPTDIR=`dirname $0`
source $SCRIPTDIR/.env
java -jar connection-monitor/target/connection-monitor-1.0-SNAPSHOT.jar $*
