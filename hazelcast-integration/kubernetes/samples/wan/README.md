# Hazelcast WAN Replication (Enterprise Only)

This a complete example presenting how to set two Hazelcast clusters (deployed in two different Kubernetes environments) with the WAN Replication in between them.

## Introduction

This example focuses on the WAN Replication feature and assumes that you have some general knowledge about Hazelcast on Kubernetes. Here are some resources:
 * [Hazelcast Kubernetes README](https://github.com/hazelcast/hazelcast-kubernetes)
 * [Hazelcast Kubernetes Code Sample](../../)
 * [Hazelcast Kubernetes Embedded Code Sample](../embedded)
   
The example also assumes you have two running Kubernetes clusters and the `kubectl` tool installed. For all the commands the indication `(Receiver)` means that `kubectl` uses the context of the receiver cluster and the indication `(Publisher)` means that `kubectl` uses the context of the publisher cluster.

**Note**: This Code Sample presents WAN Replication via LoadBalancer, which may result in the communication over only one of the target members. If you need higher performance, please check out [External Smart Client Code Sample](../external-client) and use such configuration in the WAN Replication part.  

## 1. Create Receiver Cluster

Hazelcast uses Kubernetes API for the member discovery and it therefore requires granting view permission to certain resources.

```
(Receiver) $ kubectl apply -f rbac.yaml
```

Then, you need to create a ConfigMap with the Hazelcast Configuration.

```
(Receiver) $ kubectl create configmap hazelcast-configuration --from-file=receiver/hazelcast.yaml
```

Then, create a secret with the Hazelcast Enterprise license key.

```
(Receiver) $ kubectl create secret generic hz-license-key --from-literal license=<hz-license-key>
```

Then, you can create the cluster with the following command.

```
(Receiver) $ kubectl apply -f statefulset.yaml
```

Check that cluster works correctly and note its External Load Balancer IP.

```
(Receiver) $ kubectl get all
NAME              READY     STATUS    RESTARTS   AGE
pod/hazelcast-0   1/1       Running   0          2m
pod/hazelcast-1   1/1       Running   0          2m
pod/hazelcast-2   1/1       Running   0          2m

NAME                 TYPE           CLUSTER-IP      EXTERNAL-IP      PORT(S)          AGE
service/hazelcast    LoadBalancer   10.19.240.110   35.184.122.109   5701:31112/TCP   2m
service/kubernetes   ClusterIP      10.19.240.1     <none>           443/TCP          38m

NAME                         DESIRED   CURRENT   AGE
statefulset.apps/hazelcast   3         3         2m
```

The external IP of the Hazelcast cluster is: **35.184.122.109**.

## 2. Create Publisher Cluster

Again, we need to grant Kubernetes API resource permissions.

```
(Publisher) $ kubectl apply -f rbac.yaml
```

Then, update the WAN Replication configuration in `publisher/hazelcast.yaml` with the external IP of the Receiver cluster.

```yaml
wan-replication:
  my-wan-replication:
    batch-publisher:
      my-publisher:
        cluster-name: dev
        target-endpoints: 35.184.122.109
```

Create ConfigMap with the Hazelcast configuration.

```
(Publisher) $ kubectl create configmap hazelcast-configuration --from-file=publisher/hazelcast.yaml
```

Again, we need to create a secret with the Hazelcast Enterprise license key.

```
(Publisher) $ kubectl create secret generic hz-license-key --from-literal license=<hz-license-key>
```

Finally, we can start the publisher Hazelcast cluster.

```
(Publisher) $ kubectl apply -f statefulset.yaml
```

Your two Hazelcast clusters are set up with the WAN Replication. Now, we can check if everything works correctly.

## 3. Verify WAN Replication

Insert data into the publisher Hazelcast cluster.

```
(Publisher) $ kubectl exec -it hazelcast-0 /bin/bash
# java -cp lib/hazelcast-enterprise-all*.jar com.hazelcast.client.console.ClientConsoleApp
hazelcast[default] > ns rep
namespace: rep
hazelcast[rep] > m.put key value
null
```

Check that the data was replicated to the receiver Hazelcast cluster.

```
(Receiver) $ kubectl exec -it hazelcast-0 /bin/bash
# java -cp lib/hazelcast-enterprise-all*.jar com.hazelcast.client.console.ClientConsoleApp
hazelcast[default] > ns rep
hazelcast[rep] > m.get key
value
```

The value was returned, which means that WAN Replication worked correctly.