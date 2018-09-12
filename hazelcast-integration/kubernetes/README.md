# Hazelcast for Kubernetes

This sample is a guideline on how to start Hazelcast cluster on the Kubernetes environment.

Other samples are in available in the [samples](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/kubernetes/samples) directory.

Note: For the Helm Chart installation, please refer to [Hazelcast Helm Chart](https://github.com/helm/charts/tree/master/stable/hazelcast).

## Prerequisites

1) Up and running [Kubernetes](https://kubernetes.io) version 1.9 or higher.

  * For development and testing, you may use [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/)
  * You must have the Kubernetes command line tool, [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/),
    installed

2) Another important note would be that this document assumes some familiarity with `kubectl`, Kubernetes, and Docker.

## Deployment Steps

Starting a Hazelcast cluster consists of a few steps: Creating Role Binding, Creating Config Map, Creating Secret with Enterprise Key, Starting Hazelcast Cluster.

#### Creating Role Binding

Hazelcast uses Kubernetes API to discover nodes and that is why you need to grant certain permissions. The simplest Role Binding file can look as `rbac.yaml`. Note that you can make it more specific, since Hazelcast actually uses only certain API endpoints. Note also that if you use "DNS Lookup Discovery" instead of "REST API Discovery", then you can skip the Role Binding step at all. Read more at [Hazelcast Kubernetes API Plugin](https://github.com/hazelcast/hazelcast-kubernetes).

You can apply the Role Binding with the following command:

    $ kubectl apply -f rbac.yaml

#### Creating Config Map

Hazelcast configuration can be stored in the Config Map. You can install it with the following command:

    $ kubectl apply -f config.yaml

#### Creating Secret with Enterprise Key (Enterprise Only)

Hazelcast Enterprise requires the Hazelcast Enterprise License Key. You can store it as Kubernetes Secret.

Note: This step is not required for Hazelcast Open Source.

    $ kubectl create secret generic hz-enterprise-license --from-literal=key=LICENSE-KEY-HERE

### Starting Hazelcast Cluster

Finally, deploy the Hazelcast cluster:

    $ kubectl apply -f hazelcast.yaml

For Hazelcast Enterprise, use the following command:

    $ kubectl apply -f hazelcast-enterprise.yaml

### Persistent Volume

This is a **prerequisite** step if you have JARs.

In order to share custom domain JARs (for example `EntryProcessor` implementations) among Hazelcast pods, you need to add a persistent volume in Kubernetes.

There are many different ways you can define and map volumes in Kubernetes.
Types of volumes are discussed in the [official documentation](https://kubernetes.io/docs/concepts/storage/volumes/).

Once you have created a volume, copy your custom Hazelcast JARS into the root volume directory and add it into the `CLASSPATH` environment variable.

In the following example a GCE Persistent Disk named "my-hz-disk" has been already created and populated with the
custom configuration.

* Open a text editor and add the following deployment YAML for persistent volume:

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: hz-pv
spec:
  capacity:
    storage: 10Gi
  storageClassName: standard
  accessModes:
    - ReadWriteOnce
    - ReadOnlyMany
  persistentVolumeReclaimPolicy: Retain
  gcePersistentDisk:
    pdName: my-hz-disk
    fsType: ext4
```

Save this file as `hz-pv.yaml`. Please also notice that `Reclaim Policy` is set as `Retain`. 
Therefore, contents of this folder will remain as is, between successive `claims`.

Create the persistent volume:

    $ kubectl apply -f hz-pv.yaml

Please note that contents of your previous deployment is preserved. 
If you change the claim policy to `RECYCLE`, you have to transfer all custom files to `<your-pv-path>` 
before each successive deployments.

Now edit `hazelcast.yaml` and a PersistentVolumeClaim definition to match the above PersistentVolume.
Finally, set the `CLASSPATH` env variable to a valid path and add a corresponding `volumeMount` in the StatefulSet.
