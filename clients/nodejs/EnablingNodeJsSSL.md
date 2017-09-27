# Enabling SSL/TLS for NodeJS Hazelcast Client
Hazelcast NodeJs client package supports certificate based authentication and encryption. This article looks at how to enable certificate based authentication and encryption for NodeJS hazelcast client. For continuity it is recommended to go through the previous blog on how to get started with NodeJs Hazelcast client  [Link to previous blog]. 

### SSL Options
Same as the NearCacheConfig which was dicussed in the previous article, SSLOption is also a configuration variable in the Config object which can be set with the required parameters. The SSLOption attributes are as below 

1. ca : CA name which is used to authorize the certificate OR a Buffers of trusted certificates in PEM format
2. pfx : PFX or PKCS12 file containing private keys, certificates and CA cert of the client
3. key : private key of the client in PEM format
4. passphrase : password for 'private key' OR 'pfx'
5. cert : client certificate in PEM format
6. servername : Server Name Indication
7. rejectUnautharized : If True, then the certificate is verified againts CA and on verification failure error is raised. Self signed certificate will cause error in most cases . Setting the value to false ignores the validation however does not effect encrytion.

### Creating Certificates for the example
The certificates and keys for server and client can be created as follows :

1.  Generate private key  
``` 
> openssl genrsa 1024 OR 2048 > hazelcastssl.101.key
```
2. create a certificate signing request
``` 
> openssl req -new -key ./hazelcastssl.101.key > ./hazelcastssl.101.csr 
```
3. create a self signed certificae for client apps 
``` 
> openssl x509 -in hazelcastssl.101.csr -out hazelcastssl.101.cert -req -signkey hazelcastssl.101.key -days 3600 
```
4. create a Keystore for certificate and Keys 
```
> openssl pkcs12 -export -in hazelcastssl.101.cert -inkey hazelcastssl.101.key -out hazelcastssl.101.p12
```
5. convert cert file to PEM format for nodejs 
```
> openssl x509 -in hazelcastssl.101.cert -outform PEM -out hazelcastssl.101.pem
```
 
Now you have all the required resource to set up secure connection between Nodejs client and hazelcast server.

### Starting Hazelcast node with SSL 
Security is an enterprise feature of Hazelcast hence you will need enterprise license to use the below code

```
    public class MemberSSL101 {

    public static void main(String[] args) throws Exception {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true);
        sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
        sslConfig.setProperty("keyStore", new File("enterprise/client-ssl/ssl101/hazelcastssl.101.p12").getAbsolutePath());
        sslConfig.setProperty("keyStorePassword", "password");
        
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getNetworkConfig().setSSLConfig(sslConfig);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        System.out.println("Hazelcast Member SSL instance is running!");
        }
       }
```
### NodeJs Client with SSL

```
nodejsHazelcastClientWithSSL.js

const HazelcastClient = require('hazelcast-client').Client;
const Config = require('hazelcast-client').Config;
let listener = require('./listener');
var fs = require('fs');

process.stdout.write("Dir Name : "+ __dirname);
let initConfig = (nearCache) => {
	  let config = new Config.ClientConfig();
	  config.networkConfig.addresses = [{host: '192.168.0.28', port: '5701'}];
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

```
The above code will make a secure connection with Hazelcast node and perform operations . The output will be printed as 
```
[DefaultLogger] INFO at HazelcastClient: Client started
added key: key, old value: undefined, new value: value
removed key: key, old value: somevalue, new value: undefined
```
### What happened here ?
The ssl connection configuration is  provided by the few lines below 
```
config.networkConfig.sslOptions={rejectUnauthorized: true,
	                                   ca : fs.readFileSync(__dirname+'/ssl101/hazelcastssl.101.pem'),
	                                   servername:'Hazelcast101'
                                       };
```
The certificate that is being used in the example is self-signed 'rejectUnauthorized' when set to true will generally reject self signed certificate. In this case, it is only working because the certificate is configured as a CA trusted certificate .
### Working with Self Signed certificates

The below configuration will result in error as the certificate is self-signed. This is because nodeJs will consider it as unauthorized by a CA. 
```
config.networkConfig.sslOptions={rejectUnauthorized: true,
	                                   cert : fs.readFileSync(__dirname+'/ssl101/hazelcastssl.101.pem'),
	                                   servername:'Hazelcast101'
                                       };
  Error :
    { Error: self signed certificate
    at Error (native)
    at TLSSocket.<anonymous> (_tls_wrap.js:1092:38)
    at emitNone (events.js:86:13)
    at TLSSocket.emit (events.js:185:7)
    at TLSSocket._finishInit (_tls_wrap.js:610:8)
    at TLSWrap.ssl.onhandshakedone (_tls_wrap.js:440:38) code: 'DEPTH_ZERO_SELF_SIGNED_CERT' }
                                   
```
To override the above validation the rejecUnauthorized flat will have to be set to 'false' and the above configuration will work fine. However this is not recommended as the application will be exposed to attacks.

## HAZELCAST NODE JS CLIENT RESOURCES
API Documentation & API :[Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/)

API Reference for Config Module :[Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/modules/_config_.html)

API Reference for the NearCacheConfig Element : [Click Here](http://hazelcast.github.io/hazelcast-nodejs-client/api/0.6.1/docs/classes/_config_.nearcacheconfig.html)
