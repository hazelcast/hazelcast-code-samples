# Openshift Container Platform Client Integration Sample
 
 This is a sample application that shows how to use Hazelcast client within Openshift Container Platform.
 
 This sample application uses [Kubernetes Discovery SPI](https://github.com/hazelcast/hazelcast-kubernetes) when deployed 
 to OCP.
  
  ## Prerequisites
   
   1) Install Docker Engine on your development machine from [Docker Installations](https://docs.docker.com/engine/installation/)
   2) Install Openshift Container Development Kit from [Redhat](https://developers.redhat.com/products/cdk/download/). Please note that
   downloading and installation will require Redhat subscription. Moreover, please follow CDK installation
   [document](https://access.redhat.com/documentation/en-us/red_hat_container_development_kit/2.4/html/installation_guide/)
   3) Install `oc` command line tools. [OC Tools](https://access.redhat.com/downloads/content/290/ver=3.4/rhel---7/3.4.1.10/x86_64/product-software) 
   4) Login to OCP via `oc login https://<your-ocp-domain>:8443`. You may use as admin, or a user that has required privileges to push images. 
   
   ## Sample Deployment
   
   ### Step 1
   OCP sample uses [fabric8](https://fabric8.io/) maven plugin to build Docker image, Fabric8 requires 
   3.3.x or higher maven version, therefore make sure that you have proper maven version installed on your machine.
   
   Compile and build snapshot jar file for OCP sample with below command on project root:
   ```
   mvn clean install
   ```
   Before building and pushing image to OCP, please make sure that you are in required or preferred project with:
   ```
   oc projects
   ```
   Traverse to `ocp-demo-frontend` directory under hazelcast-integration, and execute below command to build docker image:
   ```
   mvn fabric8:build
   ```
   
   To verify image in OCP Master please run below commands in shell:
   ```
   oc get is
   ```
   You should see `ocp-demo-frontend` as a repository.
  
   ### Step 2
   You may create a Hazelcast cluster on OCP using [this](https://github.com/hazelcast/hazelcast-docker/blob/master/hazelcast-openshift-origin/hazelcast-template.js) Kubernetes config json.
   Please follow OCP documentation to deploy images or Kubernetes templates to your project.
   
   ### Step 3
   Deploy OCP Sample to your project, please use Docker image that is pushed to local registry in Step 2.
   Please follow OCP documentation to deploy images or Kubernetes templates to your project.
   
   ## Step 4
   Create route for OCP sample app in your project in OCP.
   Please follow OCP [documentation](https://access.redhat.com/documentation/en-us/openshift_enterprise/3.2/html/developer_guide/dev-guide-routes) to create route to access sample app.
   Also, you should probably need to add this route your ```hosts``` file, for name resolution.
   
   ### Step 5
   Access to sample app from browser, with the route you defined in Step 5. You should see below home screen.
   
   ![welcome](markdown/images/welcome.png)
   
   ## Troubleshooting
   
   ### Unable to find Hazelcast member(s) via Kubernetes DNS Discovery
   
   OCP sample application has below `hazelcast-client.xml` configuration.
   ```
   <!--"Headless" (without a cluster IP) Services are also assigned a DNS A record for a name of the form my-svc.my-namespace.svc.cluster.local. Unlike normal Services, this resolves to the set of IPs of the pods selected by the Service. Clients are expected to consume the set or else use standard round-robin selection from the set.-->
   <!--https://github.com/kubernetes/kubernetes/tree/v1.0.6/cluster/addons/dns-->
   <property name="service-dns">hz.default.svc.cluster.local</property>
   ```
   Check your service name and namespace has compliance with above convention.
   
   ### Unable to run `Entry Processor` demo
   In order to use `EntryProcessor` demo, you should transfer `ocp-entry-processor-0.1-SNAPSHOT.jar` to OCP master where
   Hazelcast member `data` directory resides. In order to that, please follow the instructions that are defined in 
   `Creating Volume and Loading Custom Configurations` section of [this document](https://github.com/hazelcast/hazelcast-docker/tree/master/hazelcast-openshift-rhel)