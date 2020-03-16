# Hazelcast cluster on OpenShift

This repository contains the following folders:

* [Hazelcast Enterprise RHEL](hazelcast-enterprise-rhel/): provides the template to deploy Hazelcast IMDG Enterprise RHEL onto OpenShift Container Platform
* [Hazelcast Enterprise](hazelcast-enterprise/): provides the template to deploy Hazelcast IMDG Enterprise onto OpenShift Container Platform
* [Hazelcast](hazelcast/): provides the template to deploy Hazelcast IMDG onto OpenShift

# Quick start

You can start the Hazelcast application on OpenShift with the following command:

```
$ oc new-app -f hazelcast.yaml \
  -p NAMESPACE=<project_name> \
  -p ENTERPRISE_LICENSE_KEY=<hazelcast_enterprise_license>
```


