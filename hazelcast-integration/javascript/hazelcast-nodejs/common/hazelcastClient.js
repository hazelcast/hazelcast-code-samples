/**
 *  Simple JavaScript Hazelcast client wrapper
 *
 *  @author Viktor Gamov on 3/30/15.
 *  Twitter: @gamussa
 *  @since 0.0.1
 */

var java = require('java');

module.exports = function () {
    'use strict';

    var HazelcastClientClass = java.import('com.hazelcast.client.HazelcastClient');
    var ClientConfigClass = java.import('com.hazelcast.client.config.ClientConfig');
    var ArrayListClass = java.import('java.util.ArrayList');

    var clientConfig = new ClientConfigClass();
    var addresses = new ArrayListClass();
    addresses.add('127.0.0.1');
    var networkConfig = clientConfig.getNetworkConfig();
    networkConfig.setAddresses(addresses);

    var hazelcastClient = HazelcastClientClass.newHazelcastClient(clientConfig);

    var map = hazelcastClient.getMap('default');
    var myEntryListener = java.newProxy('com.hazelcast.core.EntryListener', {
        // EntryEvent<K,V> event
        entryAdded: function (event) {
            console.log('MyEntryListener => Key: ' + event.getKey() + ' Old value: ' + event.getOldValue() + ' New value: ' + event.getValue());
        },
        entryEvicted: function (event) {
        },
        entryRemoved: function (event) {
        },
        entryUpdated: function (event) {
            console.log('MyEntryListener => Key: ' + event.getKey() + ' Old value: ' + event.getOldValue() + ' New value: ' + event.getValue());
        },
        // MapEvent event
        mapCleared: function (event) {
        },
        mapEvicted: function (event) {
        }
    });
    map.addEntryListener(myEntryListener, true);
    map.put('key1', 'test1');
    console.log('Value for key1 is: ' + map.get('key1'));
};
