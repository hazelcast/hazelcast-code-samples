# Hazelcast on Amazon ECS

This is a sample Spring Boot application with embedded Hazelcast, which presents forming a Hazelcast cluster on [Amazon ECS (Elastic Container Service)](https://aws.amazon.com/ecs/).

## 1. Create AWS ECS Cluster

Open AWS ECS Console: [https://console.aws.amazon.com/ecs/home](https://console.aws.amazon.com/ecs/home). Select "Clusters" and "Create Cluster". Choose "EC2 Linux + Networking" and click "Next step".

![Configure Cluster](markdown/configure-cluster-1.png)

Note the following fields (you'll need them later for the Hazelcast configuration):
* Cluster name
* VPC CIDR block

You may also want to set "Security group inbound rules" to allow access to your Spring Boot application from outside AWS.

![Configure Cluster](markdown/configure-cluster-2.png)

When you click "Create", the cluster is created and after clicking "View Cluster", you should see that the EC2 Instances are assigned to the cluster.

![Configure Cluster](markdown/configure-cluster-3.png)

## 2. Configure Hazelcast to work on AWS ECS

You can configure Hazelcast to work on AWS by using the [hazelcast-aws](https://github.com/hazelcast/hazelcast-aws) plugin.

Add the following Maven dependency:
```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-aws</artifactId>
    <version>2.2</version>
</dependency>
```

Then, configure the AWS Discovery Strategy properties. You can do it in different two manners: Java-based configuration or XML configuration. In this code sample, we used the first approach:
```java
public Config hazelcastConfig() {
    Config config = new Config();
    config.getProperties().setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.getName(), "true");
    config.getNetworkConfig().getInterfaces().addInterface("10.0.*.*");
    JoinConfig joinConfig = config.getNetworkConfig().getJoin();
    joinConfig.getMulticastConfig().setEnabled(false);

    AwsDiscoveryStrategyFactory awsDiscoveryStrategyFactory = new AwsDiscoveryStrategyFactory();
    Map<String, Comparable> properties = new HashMap<>();
    properties.put("region", "eu-central-1");
    properties.put("tag-key", "aws:cloudformation:stack-name");
    properties.put("tag-value", "EC2ContainerService-test-cluster");
    joinConfig.getDiscoveryConfig()
              .addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(awsDiscoveryStrategyFactory, properties));

    return config;
}
``` 

The equivalent XML configuration would look as follows:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast xmlns="http://www.hazelcast.com/schema/config" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.10.xsd">
<properties>
 <property name="hazelcast.discovery.enabled">true</property>
</properties>
  <network>
    <interfaces enabled="true">
      <interface>10.0.*.*</interface>
    </interfaces>
    <join>
      <multicast enabled="false"/>
        <discovery-strategies>
            <discovery-strategy enabled="true" class="com.hazelcast.aws.AwsDiscoveryStrategy">
                <properties>
                   <property name="region">eu-central-1</property>
                   <property name="tag-key">aws:cloudformation:stack-name</property>
                   <property name="tag-value">EC2ContainerService-test-cluster</property>
                </properties>
            </discovery-strategy>
        </discovery-strategies>
    </join>
  </network>
</hazelcast>
```

Note the following parameters:
* **interface**: must be the same as "VPC CIDR block"
* **region**: must the same as the region in which your cluster is running
* **tag-key, tag-value**: ECS automatically tags EC2 instances, so you can use the tags (change "test-cluster" to the name of your cluster); if you don't specify "tag-key" and "tag-value", then all your EC2 Instances will be used to form Hazlecast cluster

## 3. Build application and Docker image

To build your application, use Maven:
```bash
mvn clean package
```

Then, you can build Docker image with the use of `Dockerfile`.
```bash
docker build -t leszko/aws-ecs-sample .
```

Please change `leszko` to your Docker Hub login.

Push the image into the registry.

```bash
docker push leszko/aws-ecs-sample
```

## 4. Create AWS ECS Task Definition

Open again AWS ECS Console and click on "Task Definitions" and "Create new Task Definition". Select "EC2" and click "Next step".

![Task Definition](markdown/task-definition-1.png)

Fill the required fields. Don't forget to set "Network Mode" to "Host" (that is the only network mode currently supported by the Hazelcast AWS Plugin).

Click "Add container", fill the required fields.

![Task Definition](markdown/task-definition-2.png)

Add logging to the container specification.

![Task Definition](markdown/task-definition-3.png)

Click "Add" and "Create" and your task definition is created.

## 5. Start AWS ECS Service

Open your cluster and in the tab "Services", click "Create".

![Create Service](markdown/create-service-1.png)

Select your task definition and the number of tasks. Click "Next step" a few times and "Create".

Your service with tasks should be running.

![Create Service](markdown/create-service-2.png)

## 6. Verify that Application works correctly

Click on any of the running tasks and scroll down to the "Log Configuration" section.

![Verify Application](markdown/verify-application-1.png)

Click "View logs in CloudWatch". You should see that the Hazelcast Members formed one cluster.

![Verify Application](markdown/verify-application-2.png)

In the real case scenario you would probably set up a load balancer to access your Spring Boot application replicas. Here, if you want to open the Spring Boot application, you need to check its port (in logs) and check the public IP of the given EC2 Instance. Then, open your browser at `http://<public_IP>:<port>/put?key=some-key&value=some-value`.

![Verify Application](markdown/verify-application-3.png)

![Verify Application](markdown/verify-application-4.png)