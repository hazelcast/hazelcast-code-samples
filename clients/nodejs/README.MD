# ENABLING NEARCACHE IN NODE JS HAZELCAST CLIENT 
Remote calls are expensive, it impacts performance. If an application has to make a remote call everytime it process a request its performance can be improved greatly by introducing smart local caching which automatically reduces the remote calls.In this article we will look at how to set up a nodejs client for hazelcast and use the Near-Cache feature of hazelcast to improve the performance of applications.
### Why Hazelcast Near Cache ?
In distributed architectures, obviously remote operations are more costly than local ones so you should try to increase local ops-to remote ops ratio in your system. Near-cache is a feature that allows you to do just that. Once enabled the client will cache objects fetched from Hazelcast cluster in a local cache such that subsequent requests can be fulfilled with object stored in the clients local cache (Near Cache). This  limits the need to fetch object from the remote cluster every time.Near Cache is highly recommended for data  that are mostly read-only or have a short lifespan.
Hazlecast client applications within your service layer be it Java,NodeJs,C# etc can cache objects and responses locally for a configurable period of time to avoid remote calls. Hazelcast also allows to implement granular control of the nearcache with ability to configure properties like timeToLive, timeToIdle, invalidateOnChange, evicton policy , preloader etc.  We will not look at setting up a simple example of Nodejs client with Near-cache.   

### Setting up Nodejs Client for Hazelcat 
Before we go futher into near cache implementation we need to set up a NodeJs client for Hazelcast. Hazelcast has a nodejs module/package called 'hazelcast-client' which can be added as a dependency and used to communicate with hazelcast cluster from a NodeJs application. The package can be installed as below ...
   ####  npm install hazelcast-client
The module can be added as a dependancy in the package.json file as below 
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
**Package.json

Now we are ready to use hazelcast in the Nodejs application.

### SPECIFYING NEARCACHE CONFIG IN NODEJS APP
The near cache is configured in nodejs application using the NearCacheConfig attribute of the Config class in Hazelcast-client node js module.

The definition of NearCacheConfig is as below :
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
For more details on these configuration element please visit documentation at http://docs.hazelcast.org/docs/latest-development/manual/html/Performance/Near_Cache/Hazelcast_Data_Structures_with_Near_Cache_Support.html

### NodeJs Code Sample with Near Cache
ExampleApp.js
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

```
1. Import Client Class
2. Import Config Class (hazelcast-client\lib\Config.js 
5. Function to create configuration
7.Provide the Network config i.e. the details of the cluster to connect to. 
10. Assign a NEW instance of NearCacheConfig element to local variable ‘orgsNearCacheConfig’
11.Set NearCacheConfig element ‘invalidateOnChange’ 
12. Set NearCacheConfig element ‘Name’ (Map Name)
14.Create an array of NearCacheConfig element ‘ncConfigs’ 
15. Assign the nearCacheConfig as the first element in ncConfigs

**Repeat the process to create more NearCache Configs 16 Set the array ncConfigs to config.nearCacheConfigs

18.Print the Configuration to Console Connecting to and Using Cluster
23.Get Hazelcast Client Instance 24 Get Distributed Map 'my-distributed-map'
25 - 31. Add listener and use the ‘promise’ feature of nodejs to sequentially add and update values in map
```
## Code Output
The near cache element of the configuration that is printed out when you run the code is as below 
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

The output of the program is as below 
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
## HAZELCAST NODE JS CLIENT RESOURCES
API Documentation & API :[Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/)

API Reference for Config Module :[Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/modules/_config_.html)

API Reference for the NearCacheConfig Element : [Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/classes/_config_.nearcacheconfig.html)
