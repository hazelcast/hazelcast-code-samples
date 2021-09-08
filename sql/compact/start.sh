#!/bin/sh

dir=`dirname "$0"`

java -cp ${dir}/target/lib/*:${dir}/target/classes NoClassExample
java -cp ${dir}/target/lib/*:${dir}/target/classes WithExistingClassExample
