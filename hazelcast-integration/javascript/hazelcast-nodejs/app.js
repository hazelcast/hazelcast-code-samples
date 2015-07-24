#!/usr/bin/env node

var java = require('java');
var hzClient = require('./common/hazelcastClient');

java.asyncOptions = {
    asyncSuffix: undefined,
    syncSuffix: ''              // Sync methods use the base name(!!)
};

var rl = require('readline').createInterface(
    process.stdin, process.stdout
);

// For node-java-maven example, refer appWithMaven.js
java.classpath.push('./lib/hazelcast-3.5.jar');
java.classpath.push('./lib/hazelcast-client-3.5.jar');

hzClient();

rl.prompt();
