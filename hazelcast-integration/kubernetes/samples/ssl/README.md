# Hazelcast with SSL (Enterprise Only)

This a complete example presenting how to use Hazelcast Enterprise with SSL enabled on the connections between Hazelcast Members and Hazelcast Client.

## Introduction

This example focuses on the security features and assumes that you have some general knowledge about:
 * Hazelcast on Kubernetes:
   * [Hazelcast Kubernetes README](https://github.com/hazelcast/hazelcast-kubernetes)
   * [Hazelcast Kubernetes Code Sample](../../)
   * [Hazelcast Kubernetes Embedded Code Sample](../embedded)
 * Java KeyStore and TrustStore
   * [Oracle's introduction to KeyStore and TrustStore](https://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html)
   * [How KeyStore and TrustStore were Generated](#how-keystore-and-truststore-were-generated)

The example also assumes you have a running Kubernetes cluster and the `kubectl` tool installed.

## 1. Creating Hazelcast Cluster

Hazelcast must have access to `hazelcast.yaml` as well as `keystore` and `truststore`, so we'll store them as `ConfigMap`/`Secret`.

```bash
$ kubectl create configmap hazelcast-configuration --from-file=server/hazelcast.yaml
$ kubectl create secret generic keystore \
                 --from-file=server/keystore \
                 --from-file=server/truststore \
                 --from-literal keystorePassword=123456 \
                 --from-literal truststorePassword=123456
```

Next, you need to create a secret with Hazelcast Enterprise License (if you don't have one, get a trial license key from this [link](https://hazelcast.com/hazelcast-enterprise-download/trial/)).

```bash
$ kubectl create secret generic hz-license-key --from-literal license=<hz-license-key>
```

Now grant access to Kubernetes API with `rbac.yaml` and deploy Hazelcast cluster to Kubernetes.

```bash
$ kubectl apply -f server/rbac.yaml
$ kubectl apply -f server/statefulset.yaml
```

You can check in the the logs that Hazelcast cluster has been formed and that SSL is enabled for the communication.

```bash
$ kubectl logs pod/hazelcast-0
...
INFO: [10.16.0.17]:5701 [dev] [4.0] SSL is enabled
...
Members {size:3, ver:3} [
        Member [10.16.0.17]:5701 - 548dd674-e8b5-4bfc-9f29-972c1d6dc3c5 this
        Member [10.16.2.10]:5701 - 8a347b50-d521-4b69-bdb3-a9e2535f40e1
        Member [10.16.0.18]:5701 - cdd2943c-3e4f-47cc-a5e0-b08da0d7cd60
]
```

## 2. Starting Hazelcast Client

As a client side, you'll deploy a Spring Boot application which connects to the Hazelcast cluster.

The application already includes the client configuration and `truststore`. All you need to do is to compile, package, create Docker image, and push it into your Docker Hub account. You may do it all with the following commands.

```bash
$ mvn clean package
$ docker build -t leszko/hazelcast-client client
$ docker push leszko/hazelcast-client
```

Please change `leszko` to your Docker Hub login. Then, make sure that your image in Docker Hub is public (you can do it on the [Docker Hub website](https://hub.docker.com/)).

Now you can edit `client/deployment.yaml` and change `leszko` to your Docker Hub account and finally deploy the Hazelcast client application to Kubernetes.

```bash
$ kubectl apply -f client/deployment.yaml
```

You can see that the client connected to the cluster using SSL by checking the logs.

```bash
$ kubectl logs pod/hazelcast-client-74d9fd74cb-cgcb4

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.2.6.RELEASE)
 
...

2018-12-11 14:12:30.077  INFO 1 --- [           main] com.hazelcast.client.ClientExtension     : SSL is enabled

...

Members [3] {
        Member [10.16.0.19]:5701 - 9e100141-7b8e-4095-9eb2-e3be46efb67c
        Member [10.16.2.11]:5701 - 17f28bee-de31-43af-8b06-87356dbef790
        Member [10.16.0.20]:5701 - cefeed37-7475-4b95-bd13-0ade587e69dd
}

...

2018-12-11 14:12:34.191  INFO 1 --- [           main] com.hazelcast.kubernetes.Application     : Started Application in 12.268 seconds (JVM running for 13.52)
```

Then, assuming you have Load Balancer configured for your Kubernetes environment, you can check the public IP of your client application.

```bash
$ kubectl get all | grep service/hazelcast-client
service/hazelcast-client   LoadBalancer   10.19.243.20   35.224.198.15   8080:30136/TCP   1m
```

Now, to check that everything works fine by executing the following commands.

```
$ curl 35.224.198.15:8080/put?key=some-key\&value=some-value
{"response":null}

$ curl 35.224.198.15:8080/get?key=some-key
{"response":"some-value"}
```

**Note**: *In the example, we didn't use Mutual Authentication, so Hazelcast client itself was not being authorized. For more information check [Mutual Authentication section](#mutual-authentication).*

## More Information

### How KeyStore and TrustStore were Generated

KeyStore and TrustStore files for this example were generated using the following commands:

```bash
$ keytool -genkey -alias client -keyalg RSA -keystore keystore -keysize 2048 -storepass 123456
What is your first and last name?
  [Unknown]:  hazelcast-mancenter
What is the name of your organizational unit?
  [Unknown]:
What is the name of your organization?
  [Unknown]:
What is the name of your City or Locality?
  [Unknown]:
What is the name of your State or Province?
  [Unknown]:
What is the two-letter country code for this unit?
  [Unknown]:
Is CN=my-release-hazelcast-enterprise-mancenter, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
  [no]:  yes
 
$ keytool -export -alias client -file client.crt -keystore keystore -storepass 123456
Certificate stored in file <client.crt>
 
$ keytool -import -v -trustcacerts -alias client -file client.crt -keystore truststore -storepass 123456
Owner: CN=my-release-hazelcast-enterprise-mancenter, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
Issuer: CN=my-release-hazelcast-enterprise-mancenter, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown
Serial number: 7c8af8f7
Valid from: Wed Nov 28 13:41:29 GMT 2018 until: Tue Feb 26 13:41:29 GMT 2019
Certificate fingerprints:
         SHA1: 0B:8B:B2:F2:BA:DA:4F:3E:88:90:A7:7E:47:4A:DE:18:BE:DD:7E:5D
         SHA256: A9:A4:EE:BB:1E:FB:A2:0F:18:D0:34:09:07:0A:63:AE:62:4E:F6:1B:A0:4F:E1:D2:6A:CD:EB:2B:91:D2:EE:29
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
 
Extensions:
 
#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: F1 CC 48 90 06 75 D0 51   1D 75 D8 E0 16 DC 66 04  ..H..u.Q.u....f.
0010: FC 4D A3 9B                                        .M..
]
]
 
Trust this certificate? [no]:  yes
Certificate was added to keystore
[Storing truststore]

$ rm client.crt
```

**Note**: *We used `hazelcast-mancenter` as the hostname, which means that if you start Management Center, its service must be named `hazelcast-mancenter` (otherwise the hostname verification fails).*

### Mutual Authentication

SSL Mutual Authentication can be enabled to increase the security. To enable it, you need to configure it in both Hazelcast Server and Hazelcast Client.

**Note**: *Currently, Mutual Authentication does not work with `livenessProbe`/`readinessProbe` enabled.*

#### Hazelcast Server

Add the following line to the `ssl` properties section (in `hazelcast.yaml`):

```yaml
mutualAuthentication: REQUIRED
```

#### Hazelcast Client

Add the following lines to the `SSLConfig` object in `hazelcastConfig()` (in the file `Application.java`):

```java
.setProperty("keyStore", "keystore")
.setProperty("keyStorePassword", System.getEnv("KEYSTORE_PASSWORD"))
```
You also need to add `keystore` into `resources` by appending the following line to `Dockerfile`:

```
COPY src/main/resources/keystore keystore
```
