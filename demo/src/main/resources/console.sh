#!/bin/sh

java -server -Djava.net.preferIPv4Stack=true -cp ../target/lib/hazelcast-all-${hazelcast.version}.jar com.hazelcast.console.ConsoleApp
