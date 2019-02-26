#!/bin/sh

dir=`dirname "$0"`

java -cp ${dir}/target/lib/*:${dir}/target/classes com.hazelcast.codesamples.cp.atomicreference.CPMember
