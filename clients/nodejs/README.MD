# Enabling Near Cache in Hazelcast Node.js Client

The remote calls are expensive, it impacts the performance. If an application has to make a remote call everytime it process a request, its performance can be improved greatly by introducing the smart local caching which automatically reduces the remote calls. In this document, we look at how to set up a Hazelcast Node.js client to use IMDG's Near Cache feature for improving the performance of applications.

### Why Hazelcast Near Cache?

In distributed architectures, the remote operations are more costly than the local ones. So, you should try to increase the local ops/remote ops ratio in your system. Near Cache is a feature that allows you to do that. Once enabled, the client caches the objects fetched from the Hazelcast cluster in a local cache such that the subsequent requests can be fulfilled with the object stored in the clients local cache (Near Cache). This  limits the need to fetch object from the remote cluster every time. Near Cache is highly recommended for data that are mostly read-only or have a short lifespan.
Hazelcast client applications (Java, Node.js, C#, etc.) within your service layer can cache the objects and responses locally for a configurable period of time to avoid the remote calls. Hazelcast also allows to implement granular control of the Near Cache with the ability to configure the properties like `timeToLive`, `timeToIdle`, `invalidateOnChange`, `evictonpolicy` and `preloader`.  We will now look at setting up a simple example of Node.js client with Near Cache.

### Setting Up Node.js Client for Hazelcast

Before we go further into the Near Cache implementation, we need to set up a Node.js client for Hazelcast. Hazelcast has a Node.js module/package called `hazelcast-client` which can be added as a dependency and used to communicate with a Hazelcast cluster from a Node.js application. The package can be installed as shown below.

```
npm install hazelcast-client
```

The module can be added as a dependency in the `package.json` file as shown below:

```
{
  "dependencies": {
     "hazelcast-client": "0.6.1", // Check link in resources section for latest version
     "express": "^4.14.0",
     "superagent": "^2.3.0"
   },
   "scripts": {
     "test": "echo \"Warn: no test specified\" && exit 1",
   }
}
```

Now we are ready to use Hazelcast in the Node.js application.

### Configuring Near Cache in a Node.js Application

Near Cache is configured in the Node.js application using the `NearCacheConfig` attribute of the `Config` class in ``hazelcast-client``s Node.js module.

The definition of `NearCacheConfig` is shown below:

```
var NearCacheConfig = (function () {
      function NearCacheConfig() {
      this.name = 'default';
      this.invalidateOnChange = true;
      this.maxIdleSeconds = 0;
      this.inMemoryFormat = InMemoryFormat.BINARY;
      this.timeToLiveSeconds = 0;
      this.evictionPolicy = EvictionPolicy.NONE;
      this.evictionMaxSize = Number.MAX_SAFE_INTEGER;
      this.evictionSamplingCount = 8;
      this.evictionSamplingPoolSize = 16;
   }
``` 

For more details on these configuration elements, see their [documentation](https://docs.hazelcast.org/docs/latest-dev/manual/html-single/#hazelcast-data-structures-with-near-cache-support).

### Node.js Code Sample with Near Cache

`ExampleApp.js`:

```
    1. const HazelcastClient = require('hazelcast-client').Client;
    2. const Config = require('hazelcast-client').Config;
    3. let listener = require('./listener');
    4. //Configuration
    5. let initConfig = (nearCache) => {
    6.     let config = new Config.ClientConfig();
    7.     config.networkConfig.addresses = [{host: '127.0.0.1', port: '5701'}];
    8. // NearCache Configuration
    9.     if (nearCache) {
    10.       let orgsNearCacheConfig = new Config.NearCacheConfig();
    11.       orgsNearCacheConfig.invalidateOnChange = true;
    12.       orgsNearCacheConfig.name = 'my-distributed-map';
    13.       // Specify multiple NearCache configs
    14.       let ncConfigs = {};
    15.       ncConfigs[orgsNearCacheConfig.name] = orgsNearCacheConfig;
    16.       config.nearCacheConfigs = ncConfigs;
    17.    }
    18.   process.stdout.write((JSON.stringify(config, null, 2)));
    19.   return config;
    20.   //end config
    21.  };
    22. //Connect to Hazelcast Cluster
    23. HazelcastClient.newHazelcastClient(initConfig(true)).then((client) => {
    24.    let map = client.getMap('my-distributed-map');
    25.    map.addEntryListener(listener, undefined, true)
    26.       .then(() => map.put('key', 'value'))
    27.       .then(() => map.get('key'))
    28.       .then(() => map.putIfAbsent('somekey', 'somevalue'))
    29.       .then(() => map.replace('key', 'somevalue', 'newvalue'))
    30.       .then(() => map.remove('key'))
    31.    ;
    32.    });
```

### Code Walkthrough

1. Import the `Client` class.
2. Import the `Config` class (hazelcast-client\lib\Config.js).
5. Function to create the configuration.
7. Provide the `Network` configuration, i.e., the details of the cluster to connect to. 
10. Assign a new instance of the `NearCacheConfig` element to the ‘orgsNearCacheConfig’ local variable.
11. Set the `NearCacheConfig` element's ‘invalidateOnChange’.
12. Set the `NearCacheConfig` element's name (map name).
14. Create an array of the `NearCacheConfig` element: ‘ncConfigs’.
15. Assign the `NearCacheConfig` as the first element in `ncConfigs`.

Repeat the process to create more Near Cache configurations.

16. Set the `ncConfigs` array to `config.nearCacheConfigs`.
18. Print the configuration to the console connecting to and using the cluster.
23. Get the Hazelcast client instance. 
24. Get the 'my-distributed-map' map.
25 - 31. Add the listener and use the Promise feature of Node.js to sequentially add and update the values in the map.

### Code Output

The Near Cache element of the configuration that is printed out when you run the code is shown below:

```
"nearCacheConfigs": {
    "my-distributed-map": {
    "name": "my-distributed-map",
    "invalidateOnChange": true,
    "maxIdleSeconds": 0,
    "inMemoryFormat": 1,
    "timeToLiveSeconds": 0,
    "evictionPolicy": 0,
    "evictionMaxSize": 9007199254740991,
    "evictionSamplingCount": 8,
    "evictionSamplingPoolSize": 16
    }
}
```

The output of the program is shown below:

```
[DefaultLogger] INFO at ClusterService: Members received.
[ Member {
   address: Address { host: 'XXX.XXX.X.XX', port: 5701, type: 4 },
   uuid: '86a4edca-9ae3-402b-abad-2c76d672e091',
   isLiteMember: false,
   attributes: {} } ]
[DefaultLogger] INFO at HazelcastClient: Client started

added key: key, old value: undefined, new value: value
removed key: key, old value: somevalue, new value: undefined
```

## Hazelcast Node.js Client Resources

[API Documentation & API](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/)

[API Reference for Config Module](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/modules/_config_.html)

[API Reference for the NearCacheConfig Element](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/classes/_config_.nearcacheconfig.html)
