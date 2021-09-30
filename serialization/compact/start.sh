#!/bin/sh

java -cp target/lib/*:target/classes CompactFullConfig
java -cp target/lib/*:target/classes CompactZeroConfig
