# OpenShift Container Platform Integration Sample

Hazelcast can be used a Caching Layer for applications deployed on [OpenShift](https://www.openshift.com/).

This sample is a complete guideline on how to set up the local OpenShift environment, start Hazelcast cluster, and finally run a sample client application.

## Step 1: Install OpenShift environment

[Minishift](https://www.openshift.org/minishift/) toolkit is used to help with running OpenShift locally. Use the following steps to set it up:

1) Install OpenShift Container Development Kit (CDK) as described [here](https://developers.redhat.com/products/cdk/download/)
2) Configure CDK and run a first Hello World OpenShift application as described [here](https://developers.redhat.com/products/cdk/hello-world/)
3) Make sure your `minishift` and `oc` tools are installed and ready to use

```
$ minishift version
minishift v1.11.0+d7f374a
CDK v3.3.0-1

$ oc version
oc v3.9.0-alpha.3+78ddc10
kubernetes v1.9.1+a0ce1bc657
features: Basic-Auth
```

## Step 2: Start Hazelcast cluster

To start a Hazelcast cluster, please follow the guidelines [here](https://github.com/leszko/hazelcast-openshift).

## Step 3: Run a sample Hazelcast client application

To run a sample Hazelcast client application, please follow the guidelines [here](client-apps).

