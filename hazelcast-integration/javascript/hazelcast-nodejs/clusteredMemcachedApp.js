#!/usr/bin/env node

var Memcached = require('memcached');

// connect to our Hazelcast memcached server on host 127.0.0.1, port 5701, 5702
// NOTE: this doesn't make big sence in a context of Hazelcast because internally stored
// memcached data in `hz_memcache_default` map which is distributed and has 1 backup
var memcached = new Memcached(["127.0.0.1:5701", "127.0.0.1:5702"]);
memcached.set("hello_world", "greetings from planet node", 1000, function (err, success) {
    'use strict';

    // check if the data was stored
    if (success) {
        console.log("Successfully stored data");
    }

    memcached.get("hello_world", function (err, success) {

        if (success !== "greetings from planet node") {
            console.log("Failed to fetched data");
        }
        process.stdout.write(success);
        memcached.end();
    });
});
