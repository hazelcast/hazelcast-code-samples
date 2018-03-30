# OpenShift Example

Hazelcast can be used a Caching Layer for applications deployed on [OpenShift](https://www.openshift.com/).

This sample is a complete guideline on how to set up the local OpenShift environment, start Hazelcast cluster, configure Management Center, and finally run a sample client application.

### Table of Contents
* [Step-by-step instruction](#step-by-step-instruction)
  * [Step 1: Install OpenShift environment](#step-1-install-openshift-environment)
  * [Step 2: Start Hazelcast cluster](#step-2-start-hazelcast-cluster)
  * [Step 3: Access Management Center (optional)](#step-3-access-management-center-optional)
  * [Step 4: Run a sample Hazelcast client application (optional)](#step-4-run-a-sample-hazelcast-client-application-optional)
* [Custom Configuration and Custom Domain JARs](#custom-configuration-and-custom-domain-jars)
* [Development Tips](#development-tips)
  * [Useful commands](#useful-commands)
  * [Local Docker images](#local-docker-images)
  * [Debugging](#debugging)

# Step-by-step instruction

## Step 1: Install OpenShift environment

[Minishift](https://www.openshift.org/minishift/) toolkit (version 3.3.0) is used to help with running OpenShift locally. Use the following steps to set it up:

1) Install OpenShift Container Development Kit (CDK) as described [here](https://developers.redhat.com/products/cdk/download/)
2) Configure CDK and run a first Hello World OpenShift application as described [here](https://developers.redhat.com/products/cdk/hello-world/)

    The Red Hat guide is complete in general, however here are a few **hints** that can save your time:
    * Complete the "Setup" section before you run `minishift setup-cdk`
    * In case of any issues with Minishift, you can enable fine level logging with the option `--show-libmachine-logs -v5`
    * In case of using Windows and Hyper-V:
      * In one of the points you need to create the virtual switch, you can use [this guide](https://docs.microsoft.com/en-us/windows-server/virtualization/hyper-v/get-started/create-a-virtual-switch-for-hyper-v-virtual-machines) to do it; then the name of the created switch is the one you need to use instead of "External (Wireless)"
      * Make sure to [add user to Hyper-V Administrator group](https://blogs.msdn.microsoft.com/virtual_pc_guy/2016/05/30/adding-yourself-to-the-hyper-v-administrators-group-with-powershell/)
      * Hyper-V does not well support NAT networks, so your router must accept the Virtual Machine connecting directly to the same network interface (in case of problems, you may see a meaningless error `Too many retries waiting for SSH to be available`); the solution is to use VirutalBox instead of Hyper-V or play with the [experimental Minishift features](https://docs.openshift.org/latest/minishift/using/experimental-features.html)


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

Note that, in case of [Hazelcast Enterprise OpenShift Centos](hazelcast-cluster/hazelcast-enterprise-openshift-centos/), you will need a valid license key for the Hazelcast Enterprise version. If you don't have one, you can either use [Hazelcast OpenShift Origin](hazelcast-cluster/hazelcast-openshift-origin/) or get a trial key from [this link](https://hazelcast.com/hazelcast-enterprise-download/trial/).

**1) Create Project**

Make sure you are logged into the OpenShift Platform.
```
$ oc login -u developer -p developer
Login successful.
```

Then, you can create a new project.
```
$ oc new-project hazelcast
```

Note that the name of the project is automatically its namespace, so you need to use `hazelcast` as the namespace in the further steps.

**2) Start Hazelcast cluster**

Change the directory to Hazelcast Enterprise (`$ cd hazelcast-cluster/hazelcast-enterprise-openshift-centos`) or Hazelcast Open Source (`$ cd hazelcast-cluster/hazelcast-openshift-origin`).

```
$ oc new-app -f hazelcast-template.json \
  -l name=hazelcast-cluster-1 \
  -p NAMESPACE=hazelcast \
  -p ENTERPRISE_LICENSE_KEY=<hazelcast_enterprise_license>
```

Note that the label 'hazelcast-cluster-1', even though not mandatory, is helpful to manage all resources related to the created application.

Used parameters:
* `NAMESPACE`: must be the same as the OpenShift project's name
* `ENTERPRISE_LICENSE_KEY`: Hazelcast Enterprise License (not needed for [Hazelcast OpenShift Origin](hazelcast-cluster/hazelcast-openshift-origin/))

You can check other available parameters in `hazelcast-template.json`, the most interesting ones are related to Persistent Volumes:
* `HAZELCAST_VOLUME_NAME`: Persistent Volume used for Hazelcast Home Directory (`pv0001` by default)
* `MC_VOLUME_NAME`: Persistent Volume used for Management Center Data Directory (`pv0002` by default)

Minishift comes with predefined Persistent Volumes (pv0001, pv0002, ..., pv0100). In order to create a new Persistent Volume please follow the description [here](https://developers.redhat.com/blog/2017/04/05/adding-persistent-storage-to-minishift-cdk-3-in-minutes/).

**3) Check that Hazelcast is running**

To check all created OpenShift resources, use the following command.

```
$ oc get all
NAME             READY     STATUS    RESTARTS   AGE
po/hz-rc-5pl4f   1/1       Running   0          3m
po/hz-rc-dfz84   1/1       Running   0          3m
po/hz-rc-pjps7   1/1       Running   0          3m
po/mc-rc-w5d5l   1/1       Running   0          3m

NAME       DESIRED   CURRENT   READY     AGE
rc/hz-rc   3         3         3         3m
rc/mc-rc   1         1         1         3m

NAME                          TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
svc/hzservice                 ClusterIP   None         <none>        5701/TCP   3m
svc/management-center-service None        None         <none>        8080/TCP   3m
```

Please check that the `STATUS` is `Running` for all PODs. Then, to check the logs for each replica, use the following command:

```
$ oc logs po/hz-rc-5pl4f

...
Kubernetes Namespace: hazelcast
Kubernetes Service DNS: hzservice.hazelcast.svc.cluster.local
########################################
# RUN_JAVA=
# JAVA_OPTS=
# CLASSPATH=/data/hazelcast/*:/opt/hazelcast/*:/opt/hazelcast/external/*:
########################################
...
Members [3] {
        Member [172.17.0.3]:5701 - b047e291-ebd6-4edc-8b9c-d06fcb3b9965
        Member [172.17.0.4]:5701 - f5e6cf50-d83a-42c5-b152-2571a929fd12
        Member [172.17.0.2]:5701 - a50f8468-0852-45d2-966a-74301e04d45e this
}
...

```

**4) Delete Hazelcast cluster**

To delete all resources related to the cluster (Replication Controller, Service, PODs) use the following command:

```
$ oc delete all -l name=hazelcast-cluster-1
replicationcontroller "hz-rc" deleted
service "hzservice" deleted
```

You can also delete the Persistent Storage Claim by:

```
$ oc delete pvc hz-vc && oc delete pvc mv-vc
```

If you don't do it, then the next time you run your application, you will see a message: 
```
error: persistentvolumeclaims "hz-vc" already exists
error: persistentvolumeclaims "mc-vc" already exists
--> Failed
```

In such case (even though the message says `Failed`), the cluster is created and the already-existing storage is re-used.

## Step 3: Access Management Center (optional)

Management Center application (Hazelcast Enterprise only) is already started together with Hazelcast members when using `hazelcast-template.json`. Nevertheless, in order to make it usable, you need to perform the following steps.

**1) Add Management Center to Hazelcast configuration**

In order to connect Hazelcast nodes to Management Center, they need to use the custom `hazelcast.xml` configuration file with the `management-center` entry (as described [here](http://docs.hazelcast.org/docs/management-center/3.8.3/manual/html/Deploying_and_Starting.html)). You can copy the already prepared configuration into the used Persistent Volume with the following commands:

```
$ scp -i $HOME/.minishift/machines/minishift/id_rsa hazelcast.xml docker@$(minishift ip):/mnt/sda1/var/lib/minishift/openshift.local.pv/pv0001/
```

If your cluster is already started, you need to restart the PODs. There are many ways to do it, for example, you can terminate the existing PODs (and they will be automatically restarted).

```
$ oc get pods
NAME          READY     STATUS    RESTARTS   AGE
hz-rc-4pnnr   1/1       Running   0          41m
hz-rc-9mlqj   1/1       Running   0          41m
hz-rc-lht7j   1/1       Running   0          41m
mc-rc-l86cw   1/1       Running   0          41m

$ oc delete po/hz-rc-4pnnr po/hz-rc-9mlqj po/hz-rc-lht7j
pod "hz-rc-4pnnr" deleted
pod "hz-rc-9mlqj" deleted
pod "hz-rc-lht7j" deleted
```

**2) Expose Management Center**

To make Management Center accessible from outside of its container, use the following command:

```
$ oc expose svc/management-center-service
```

Then, it's accessible via the exposed route, which you can check by:
```
$ oc get route
NAME                        HOST/PORT                                                  PATH      SERVICES                    PORT      TERMINATION   WILDCARD
management-center-service   management-center-service-hazelcast.192.168.1.113.nip.io             management-center-service   8080                    None
```

Then, you can access Management Center by opening `management-center-service-hazelcast.192.168.1.113.nip.io/mancenter` in your browser.

## Step 4: Run a sample Hazelcast client application (optional)

If you're interested not only in setting up the Hazelcast cluster, but also in using it in the client application, you can follow the following guidelines.

Note that OpenShift sample uses the [fabric8](https://fabric8.io/) maven plugin to build Docker image. Fabric8 requires 3.3.x or higher maven version, therefore make sure that you have proper maven version installed on your machine.

**1) Build Maven dependencies**

Install the snapshot JAR files from the root directory:
```
$ mvn -f ../../pom.xml clean install
```

**2) Build "ocp-demo-frontend" Docker image**

Note also that in order to build the Docker image, you need to have your OpenShift Docker Engine configured. In case of Minishift, you can do it using the guidelines from the `minishift docker-env` command, so in case of Unix-based systems:

```
$ eval $(minishift docker-env)
```

Run the following command to build the Docker image:
```
$ mvn -f client-apps/ocp-demo-frontend/pom.xml fabric8:build
```

**3) Push Docker image to the local OpenShift registry**

Check if your image is already in the OpenShift registry.

```
$ oc get is
NAME                DOCKER REPO                                   TAGS      UPDATED
ocp-demo-frontend   172.30.1.1:5000/hazelcast/ocp-demo-frontend   latest    39 seconds ago
```

In case you see the message `No resources found`, you need to manually push the image with the following command:
```
$ docker login -u developer -p $(oc whoami -t) $(minishift openshift registry)
$ docker tag client-apps/ocp-demo-frontend $(minishift openshift registry)/$(oc project -q)/ocp-demo-frontend
$ docker push $(minishift openshift registry)/$(oc project -q)/ocp-demo-frontend
```

Then, you should see `oc-demo-frontend` in the output for `$ oc get is`.

**4) Start "ocp-demo-frontend" application**

To start the application you can use the following command:
```
$ oc new-app --image-stream=ocp-demo-frontend --name=hazelcast-client-app -l name=hazelcast-client-app-1
```

You can check that the application is running correctly:
```
$ oc get all -l name=hazelcast-client-app-1
NAME                                     REVISION   DESIRED   CURRENT   TRIGGERED BY
deploymentconfigs/hazelcast-client-app   1          1         1         config,image(ocp-demo-frontend:latest)

NAME                              READY     STATUS    RESTARTS   AGE
po/hazelcast-client-app-1-rcrfx   1/1       Running   0          2m

NAME                        DESIRED   CURRENT   READY     AGE
rc/hazelcast-client-app-1   1         1         1         2m

NAME                       TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
svc/hazelcast-client-app   ClusterIP   172.30.64.173   <none>        8080/TCP,8778/TCP,9779/TCP   2m
```

**5) Expose the application**

In order to make the application accessible from outside the OpenShift environment, you need to expose it using the following command:
```
$ oc expose svc/hazelcast-client-app
```

Then, you should be able to access the application via the exposed route. You can check the route using the following command:

```
$ oc get routes
NAME                   HOST/PORT                                             PATH      SERVICES               PORT       TERMINATION   WILDCARD
hazelcast-client-app   hazelcast-client-app-hazelcast.192.168.2.123.nip.io             hazelcast-client-app   8080-tcp                 None
```

Now, if you open in the browser `hazelcast-client-app-hazelcast.192.168.2.123.nip.io`, you should see the following home screen.

![welcome](markdown/images/welcome.png)

You can check that the application is really working together with the Hazelcast cluster by doing some operation in the application, for example, entering "12" in the "Data Operations->Count" and clicking "Auto Pilot". Then, in the Management Center application, you should see that the entries are added.

![management_center](markdown/images/management_center.png)

**6) Enable Entry Processor (optional)**

If you want to play with "Entry Processor" from the code sample, you need to copy the Entry Processor JAR into the Persistent Volume of Hazelcast cluster.

```
$ scp -i $HOME/.minishift/machines/minishift/id_rsa client-apps/ocp-entry-processor/target/ocp-entry-processor-0.1-SNAPSHOT.jar docker@$(minishift ip):/mnt/sda1/var/lib/minishift/openshift.local.pv/pv0001/
```

Then, after restarting all PODs in the Hazelcast cluster, you can play with "Entry Processor".

# Custom Configuration and Custom Domain JARs

In order to use a custom Hazelcast configuration (or custom domain JARs), you need to copy them into the Persistent Volume used in the application. Since the Persistent Volume is located inside the Minishift VM, you can do it using the following command:

```
$ scp -i $HOME/.minishift/machines/minishift/id_rsa hazelcast.xml docker@$(minishift ip):/mnt/sda1/var/lib/minishift/openshift.local.pv/pv0001/
```

Short explanation of the command above:
* `$HOME/.minishift/machines/minishift/id_rsa` - ssh key to Minishift VM is stored in the Minishift's home directory
* `hazelcast.xml` - custom configuration of Hazelcast
* `minishift ip` - command to return the IP address of the Minishift VM
* `/mnt/sda1/var/lib/minishift/openshift.local.pv/pv0001/` - location of the Persistent Volume `pv0001` in Minishift VM

The other possibility to put a configuration inside the Minishift VM is to share a directory with the host system using [Minishift hostfolder](https://docs.openshift.org/latest/minishift/using/host-folders.html).

One more possibility for using custom Hazelcast configuration is to [create a ConfigMap](https://docs.openshift.com/enterprise/3.2/dev_guide/configmaps.html) with the key "hazelcast.xml" and the value with the file content and to mount it instead of using Persistent Volume.

After starting the application again, the containers use the custom Hazelcast configuration.

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
