#!/bin/sh

dir=`dirname "$0"`

java -ea -cp ${dir}/target/lib/*:${dir}/target/classes com.hazelcast.codesamples.cp.RemoveCrashedCPMembersManually
