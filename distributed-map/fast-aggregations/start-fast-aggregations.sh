#!/bin/sh

export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64
JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64 mvn clean package -am
time $JAVA_HOME/bin/java -Xmx8g -cp target/lib/*:target/classes FastAggregationsDemo
