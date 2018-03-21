# OpenShift Container Platform Integration Sample

Hazelcast can be used a Caching Layer for applications deployed on [OpenShift](https://www.openshift.com/).

This sample is a complete guideline on how to set up the local OpenShift environment, start Hazelcast cluster, and finally run a sample client application.

## Step 1: Install OpenShift environment

[Minishift](https://www.openshift.org/minishift/) toolkit (version 3.3.0) is used to help with running OpenShift locally. Use the following steps to set it up:

1) Install OpenShift Container Development Kit (CDK) as described [here](https://developers.redhat.com/products/cdk/download/)
2) Configure CDK and run a first Hello World OpenShift application as described [here](https://developers.redhat.com/products/cdk/hello-world/)

The Red Hat guide is complete, however a few hints that can save your time:
* Complete the "Setup" section before you run `minishift setup-cdk`
* In case of using Windows and Hyper-V:
 * In one of the points you need to create the virtual switch, you can use [this guide](https://docs.microsoft.com/en-us/windows-server/virtualization/hyper-v/get-started/create-a-virtual-switch-for-hyper-v-virtual-machines) (then the name of the created switch is the one you need to use as "External (Wireless)")
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

To start a Hazelcast cluster, please follow the guidelines [here](https://github.com/leszko/hazelcast-openshift).

## Step 3: Run a sample Hazelcast client application

To run a sample Hazelcast client application, please follow the guidelines [here](client-apps).

