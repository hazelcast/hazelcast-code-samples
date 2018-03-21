# OpenShift Example

Hazelcast can be used a Caching Layer for applications deployed on [OpenShift](https://www.openshift.com/).

This sample is a complete guideline on how to set up the local OpenShift environment, start Hazelcast cluster, and finally run a sample client application.

### Table of Contents
- [Step-by-step instruction](#step-by-step-instruction)
- [Development Tips](#development-tips)

# Step-by-step instruction

## Step 1: Install OpenShift environment

[Minishift](https://www.openshift.org/minishift/) toolkit (version 3.3.0) is used to help with running OpenShift locally. Use the following steps to set it up:

1) Install OpenShift Container Development Kit (CDK) as described [here](https://developers.redhat.com/products/cdk/download/)
2) Configure CDK and run a first Hello World OpenShift application as described [here](https://developers.redhat.com/products/cdk/hello-world/)

    The Red Hat guide is complete in general, however here are a few hints that can save your time:
    * Complete the "Setup" section before you run `minishift setup-cdk`
    * In case of using Windows and Hyper-V:
      * In one of the points you need to create the virtual switch, you can use [this guide](https://docs.microsoft.com/en-us/windows-server/virtualization/hyper-v/get-started/create-a-virtual-switch-for-hyper-v-virtual-machines) to do it; then the name of the created switch is the one you need to use as "External (Wireless)"
      * Make sure to [add user to Hyper-V Administrator group](https://blogs.msdn.microsoft.com/virtual_pc_guy/2016/05/30/adding-yourself-to-the-hyper-v-administrators-group-with-powershell/)


3) Make sure your `minishift` and `oc` tools are installed and ready to use

```
$ minishift version
minishift v1.11.0+d7f374a
CDK v3.3.0-1

$ oc version
oc v3.7.14
kubernetes v1.7.6+a08f5eeb62
features: Basic-Auth
```

## Step 2: Start Hazelcast cluster

To start a Hazelcast cluster, please follow the guidelines [here](hazelcast-cluster).

## Step 3: Run a sample Hazelcast client application

To run a sample Hazelcast client application, please follow the guidelines [here](client-apps).

# Development Tips

## Useful commands

The complete guide to the `oc` CLI tool can be found [here](https://docs.openshift.org/latest/cli_reference/index.html). Below you can see the most interesting use cases in the context of Hazelcast.

**Scaling application**

To scale the Hazelcast application, you can change the number of replicas in the Replication Controller. For example, to scale up to 5 replicas, use the following command:

```
$ oc scale rc/hz-rc --replicas=5
```

**Exposing application**

By default, the Hazelcast cluster is accessible only from the OpenShift environment. You can, however, make it accessible from outside.

```
$ oc expose svc/hzservice
route "hzservice" exposed
```

Then, you should be able to access Hazelcast via the exposed route (you can check what the route is by `oc status` or `oc get routes/hzservice`). For example, to check the health of Hazelcast:

```
$ curl hzservice-hazelcast.192.168.1.113.nip.io/hazelcast/health
Hazelcast::NodeState=ACTIVE
Hazelcast::ClusterState=ACTIVE
Hazelcast::ClusterSafe=TRUE
Hazelcast::MigrationQueueSize=0
Hazelcast::ClusterSize=1
```

## Local Docker images

During the development process, a very common use case is to build locally own Docker images and run them on Minishift. For example, you may want to create a separate application on top of the Hazelcast OpenShift image and check if it works, or you may want to create a seprate application and check how it interacts with Hazelcast when deployed together on OpenShift.

Minishift is provided together with Docker Engine and Docker Registry. 

**1) Configure access to Docker Engine**

```
$ minishift docker-env
export DOCKER_TLS_VERIFY="1"
export DOCKER_HOST="tcp://192.168.99.101:2376"
export DOCKER_CERT_PATH="/home/rafal/.minishift/certs"
export DOCKER_API_VERSION="1.24"
# Run this command to configure your shell:
# eval $(minishift docker-env)
```

**2) Push into Minishift Docker Registry**

The following commands push the image into Minishift Docker Registry. More details can be found [here](https://docs.openshift.org/latest/minishift/openshift/openshift-docker-registry.html).

```
$ docker login -u developer -p $(oc whoami -t) $(minishift openshift registry)
$ docker tag my-app $(minishift openshift registry)/myproject/my-app
$ docker push $(minishift openshift registry)/myproject/my-app
```

Then the application can be started on the OpenShift cluster with:
```
$ oc new-app --image-stream=my-app --name=my-app
```

## Debugging

Debbuging containerized applications in the OpenShift cluster can be difficult. In order to attach to the running POD, you can use the following command:

```
oc exec -ti <pod_name> -- bash
```
