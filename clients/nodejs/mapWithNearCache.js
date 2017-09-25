/**
 * http://usejsdoc.org/
 */
const HazelcastClient = require('hazelcast-client').Client;
const Config = require('hazelcast-client').Config;
let listener = require('./listener');

//let config = new Config.ClientConfig();
//config.networkConfig.addresses = [{host: '127.0.0.1', port: '5701'}];



let initConfig = (nearCache) => {
	  let config = new Config.ClientConfig();
	  config.networkConfig.addresses = [{host: '127.0.0.1', port: '5701'}];


	  //region NearCache
	  if (nearCache) {
	    let orgsNearCacheConfig = new Config.NearCacheConfig();
	    orgsNearCacheConfig.invalidateOnChange = true;
	    orgsNearCacheConfig.name = 'my-distributed-map';

	    let ncConfigs = {};
	    ncConfigs[orgsNearCacheConfig.name] = orgsNearCacheConfig;
	    config.nearCacheConfigs = ncConfigs;
	  }
	  process.stdout.write("Config Start ==> \n");
	//  process.stdout.write((JSON.stringify(config, null, 2)));
	  process.stdout.write("Config End  ==> \n");
	  return config;
	  //endregion
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
