#!/bin/sh

java -server -Djava.net.preferIPv4Stack=true -cp ../target/demo-${project.version}.jar:../target/lib/hazelcast-all-${hazelcast.version}.jar com.hazelcast.demo.SimpleMapTest $@
