# Hazelcast cluster on OpenShift

This repository contains the following folders:

* [Hazelcast Enterprise OpenShift RHEL](hazelcast-enterprise-openshift-rhel/): provides the template to deploy Hazelcast IMDG Enterprise RHEL onto OpenShift Container Platform
* [Hazelcast Enterprise OpenShift Centos](hazelcast-enterprise-openshift-centos/): provides the template to deploy Hazelcast IMDG Enterprise onto OpenShift Container Platform
* [Hazelcast OpenShift Origin](hazelcast-openshift-origin/): provides the template to deploy Hazelcast IMDG onto OpenShift

# Quick start

You can start the Hazelcast application on OpenShift with the following command:

```
$ oc new-app -f hazelcast.yaml \
  -p NAMESPACE=<project_name> \
  -p ENTERPRISE_LICENSE_KEY=<hazelcast_enterprise_license>
```


