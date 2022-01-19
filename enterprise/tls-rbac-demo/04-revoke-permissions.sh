#!/bin/bash

java -cp target/tls-rbac-demo-*.jar com.hazelcast.samples.rbac.ReplacePermissions resources/no-permissions.xml
