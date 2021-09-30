#!/bin/sh

java -Djava.net.preferIPv4Stack=true -cp ../target/lib/hazelcast-${hazelcast.version}.jar:../target/lib/jline-${jline.version}.jar com.hazelcast.client.console.ClientConsoleApp
