#!/usr/bin/env node

var Memcached = require('memcached');
var memcached = new Memcached('localhost:5701');
var lifetime = 86400; //24hrs
memcached.set('hello', 'world', lifetime, function (err, result) {
    if (err) console.error(err);
    console.dir(result);
});
memcached.get('hello', function (err, result) {
    if (err) console.error(err);
    console.dir(result);
});
