# hazdb

When is a DB not a DB ?

#### Instructions

Build with

```
mvn clean install dockerfile:build
```

Run at least server 0. Add servers 1 and 2 if your machine can cope.

```
src/main/scripts/docker-hazelcast-server-0.sh
src/main/scripts/docker-hazelcast-server-1.sh
src/main/scripts/docker-hazelcast-server-2.sh
```

Run a client

```
src/main/scripts/docker-hazelcast-client.sh
```

Then open a browser on [http://127.0.0.1:8080](http://127.0.0.1:8080)

#### Useful

[Hazelcast JDBC Driver](https://hazelcast.com/blog/jdbc-driver-4-2-is-released/)

#### Management Center

Free to run for up to 3 nodes, but optional.

You can run it from the downloaded version, but it will default to port 8080 so clash with the client.
If you do it this way, use

```
hz-mc start -Dhazelcast.mc.http.port=8081 
```

Add the cluster connection with the cluster name "hazdb" and the IP address the cluster is on.
If you can't find this, it'll be listed in the client logs if the client has connected.

Or use the Docker script,

```
src/main/scripts/docker-hazelcast-management-center.sh
```

and then you can access on [http://127.0.0.1:8081](http://127.0.0.1:8081)

## Running on Kubernetes

If you have Kubernetes, run the three scripts

```
kubectl create -f src/main/resources/kubernetes-1.yaml
kubectl create -f src/main/resources/kubernetes-2.yaml
kubectl create -f src/main/resources/kubernetes-3.yaml
```

You may need to adjust the `image:` and `imagePullPolicy:` sections appropriate to your Kubernetes variant.



