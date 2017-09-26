
const HazelcastClient = require('hazelcast-client').Client;
const Config = require('hazelcast-client').Config;
let listener = require('./listener');
var fs = require('fs');

process.stdout.write("Dir Name : "+ __dirname);
let initConfig = (nearCache) => {
	  let config = new Config.ClientConfig();
	  config.networkConfig.addresses = [{host: '192.168.0.28', port: '5701'}];
    // SSL Config
	  config.networkConfig.sslOptions={rejectUnauthorized: true,
	                                   ca : fs.readFileSync(__dirname+'/ssl101/hazelcastssl.101.pem'),
	                                   servername:'Hazelcast101'
                                       };
	  if (nearCache) {
	    let orgsNearCacheConfig = new Config.NearCacheConfig();
	    orgsNearCacheConfig.invalidateOnChange = true;
	    orgsNearCacheConfig.name = 'my-distributed-map';

	    let ncConfigs = {};
	    ncConfigs[orgsNearCacheConfig.name] = orgsNearCacheConfig;
	    config.nearCacheConfigs = ncConfigs;
	  }
	  return config;
    };



HazelcastClient.newHazelcastClient(initConfig(true)).then((client) => {
    let map = client.getMap('my-distributed-map');
    map.addEntryListener(listener, undefined, true)
        .then(() => map.put('key', 'value'))
        .then(() => map.get('key'))
        .then(() => map.putIfAbsent('somekey', 'somevalue'))
        .then(() => map.replace('key', 'somevalue', 'newvalue'))
        .then(() => map.remove('key'))
    ;
});
