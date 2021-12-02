#!/bin/bash

java -cp target/tls-rbac-demo-*.jar com.hazelcast.samples.rbac.TimestampClient ${1:-resources/regular-hazelcast-client.xml}
