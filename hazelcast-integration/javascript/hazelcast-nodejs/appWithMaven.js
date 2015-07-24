#!/usr/bin/env node

var java = require('java');
var mvn = require('node-java-maven');
var hzClient = require('./common/hazelcastClient');

java.asyncOptions = {
    asyncSuffix: undefined,
    syncSuffix: ''              // Sync methods use the base name(!!)
};

var rl = require('readline').createInterface(
    process.stdin, process.stdout
);

// node-java-maven resolves dependencies https://github.com/joeferner/node-java-maven
mvn({debug: true}, function (err, mvnResults) {
    if (err) {
        return console.error('could not resolve maven dependencies', err);
    }
    mvnResults.classpath.forEach(function (c) {
        console.log('adding ' + c + ' to classpath');
        java.classpath.push(c);
    });

    hzClient();
});

rl.prompt();
