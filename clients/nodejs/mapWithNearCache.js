const HazelcastClient = require('hazelcast-client').Client;
const Config = require('hazelcast-client').Config;
const listener = require('./listener');


const initConfig = (nearCache) => {
      const config = new Config.ClientConfig();
	  config.networkConfig.addresses = ['127.0.0.1:5701'];

	  if (nearCache) {
        const orgsNearCacheConfig = new Config.NearCacheConfig();
	    orgsNearCacheConfig.invalidateOnChange = true;
	    orgsNearCacheConfig.name = 'my-distributed-map';

	    const ncConfigs = {};
	    ncConfigs[orgsNearCacheConfig.name] = orgsNearCacheConfig;
	    config.nearCacheConfigs = ncConfigs;
	  }
	  return config;
	};



HazelcastClient.newHazelcastClient(initConfig(true)).then((client) => {
    const map = client.getMap('my-distributed-map');
    map.addEntryListener(listener, undefined, true)
        .then(() => map.put('key', 'value'))
        .then(() => map.get('key'))
        .then(() => map.putIfAbsent('somekey', 'somevalue'))
        .then(() => map.replace('key', 'somevalue', 'newvalue'))
        .then(() => map.remove('key'))
    ;
});
