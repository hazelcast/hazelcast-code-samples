# Overview

This connector enables the use of Hazelcast Pipelines to service REST/HTTP Requests.  One major benefit of this approach is 
the ability to have multiple implementations of a service and control the amount of traffic directed to each, enabling 
blue/green style deployments.  It also enables web service implementations to be managed within Hazelcast in the same 
way that event processing workloads are managed, thus simplifying operations.

Some additional advantages of this approach are:
- The Spring Boot application is very light and stateless.  It is easy to scale simply by running multiple instances behind a load balancer.
- The Spring Boot application servers contain no business logic or data, making them safer to run in an internet facing subnet.
- The business logic is in an independently deployable Hazelcast pipeline.  The logic can be updated by deploying a new Pipeline, without touching the web servers.
- The web tier and the business logic tiers scale independently.
- Compute resources do not have to be specifically assigned to each service.  Instead, all service implementations 
  share the compute resources of a Hazelcast cluster.  Adding compute capacity is as easy as adding servers to the 
  cluster.

# Running the Example
A sample spring boot application that uses the Hazelcast pipeline dispatcher is included 
in this project.  To run it with Docker, run 

```
mvn clean install
docker compose --profile clientserver up -d
```

This will start a single-node Hazelcast pipeline running a string-reversing job and 
a Spring Boot web service that accepts HTTP GET requests.  It can be invoked with 
a URL like: `http://localhost:8080/reverse?input=helloworld`. 

To stop everything run: `docker compose --profile clientserver down`

You can also run the example with the Hazelcast instance embedded in the web application: 

`docker compose --profile embedded up -d`.

Both the *embedded* profile and the *clientserver* profile start a Hazelcast Management Center, which can be accessed 
at *http://localhost:8888*.

# Usage

To use Hazelcast as a service grid, you need to do the following 3 things.
- Implement a web service with Spring Boot.  The web service is very thin and typically contains little or no business
  logic.
- Implement the business logic in Java with a Hazelcast Pipeline.
- Deploy the pipeline and configure routing to enable your implementation.  If there are multiple implementations 
  of your pipeline, you can configure the percentage of traffic that is passed to each.

Each of the three steps is described in more detail below

## Implement The Web Service

Include the following maven dependency in your project (replace VERSION with the version you wish to use).
```xml
<dependency>
    <groupId>hazelcast.platform.solutions</groupId>
    <artifactId>spring-hazelcast-pipeline-dispatcher</artifactId>
    <version>VERSION</version>
</dependency>
```

In your REST controller, add an autowired instance of `PipelineDispatcherFactory`.

```java
import hazelcast.platform.solutions.pipeline.dispatcher.PipelineDispatcherFactory;
...

@RestController
public class ExampleService {
    @Autowired
    PipelineDispatcherFactory pipelineDispatcherFactory;
    //...
}
```
Dispatch a request to Hazelcast using code similar to the following.

```java
@RestController
public class ExampleService {
    @Autowired
    PipelineDispatcherFactory pipelineDispatcherFactory;
    
    @GetMapping("/reverse")
    public DeferredResult<String> stringReverseService(@RequestParam String input) {
        return pipelineDispatcherFactory.<String, String>dispatcherFor("reverse").send(input);
    }
}
```
The *dispatcherFor* method takes the name of the service as an argument. 

The *dispatcherFor* method has two type parameters.  The first is the type of the input and the second is the type 
of the output.  In this example, the input and output types are both Strings.  These should 
match with the types expected by and produced by the pipeline that implements the service.


### Configuring the Connection to Hazelcast

The web service is configured using properties from the Spring Environment per the usual Spring Boot
mechanism. There are several different ways to provide values for these properties.  See, for example, 
https://www.baeldung.com/properties-with-spring for more details. The properties
used by the pipeline dispatcher are given below.

| Property                                            | Description                                                                                                                                                                          |
|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hazelcast.pipeline.dispatcher.embed_hazelcast       | Whether to start a Hazelcast instance embedded in the application server (true) or connect to a remote instance (false). Defaults to false.                                          |
| hazelcast.pipeline.dispatcher.request_timeout_ms    | The number of milliseconds to wait for a response from the pipeline.  A timeout response will be returned if the response does not arrive after this amount of time. Defaults to 3s. |

Additionally, you need to set the path to a Hazelcast configuration file.  

For the client-server configuration, you can either put a file called *hazelcast-client.xml* on the class path or set
the *hazelcast.client.config* system property to the location of your configuration files.  For more details on 
configuring the connection to Hazelcast in client-server mode, see 
https://docs.hazelcast.com/hazelcast/5.2/clients/java#configuring-java-client.

For embedded mode, you can put a *hazelcast.xml* or *hazelcast.yaml* file in the working directory or on the classpath.
Alternatively, you can specify the location of a configuration file using the *hazelcast.config* system property. For 
more information about configuring Hazelcast in embedded mode, see 
https://docs.hazelcast.com/hazelcast/5.2/configuration/understanding-configuration#configuration-precedence.

> **NOTE** 
> *hazelcast.client.config* and *hazelcast.config* must be configured as system properties.  Hazelcast will not use the 
> Spring Environment abstraction for these.

### Embedding a Hazelcast Instance
For most use cases it makes more sense connect to a remote Hazelcast cluster.  However, it is possible to embed a 
Hazelcast instance into each web service instance and to have them form a cluster.

> **Note**
> Embedding is not generally recommended because, as stateful services, Hazelcast clusters must be managed with 
> more care than stateless web service instances.  For example, if the cluster data structures are configured for 1 
> redundant copy then data (e.g. requests in flight) will be lost if more than one instances is taken out of service at 
> the same time.

To embed a Hazelcast instance, set the *hazelcast.pipeline.dispatcher.embed_hazelcast* property to *true*, and provide
a hazelcast configuration file using one of the methods discussed above.  In this case, the application can deploy a 
pipeline using code similar to the following.

```java
@RestController
public class ExampleService {
    @Autowired
    PipelineDispatcherFactory pipelineDispatcherFactory;
    
    @Value("${hazelcast.pipeline.dispatcher.embed_hazelcast:false}")
    boolean embedHazelcast;
    
    // handler code here

    @PostConstruct
    public void init() {
        if (embedHazelcast) {
            HazelcastInstance hz = pipelineDispatcherFactory.getEmbeddedHazelcastInstance();
            Pipeline pipeline = ExamplePipeline.createPipeline("reverse_request", "reverse_response");
            hz.getJet().newJob(pipeline);
        }
    }
}
```

## Implement The Hazelcast Pipeline

Services are implemented as Hazelcast Pipelines.  

Pipelines that implement services must follow these guidelines.  
1. They  must read the request from an *IMap* backed *StreamSource* created using *Sources.mapJournal*
2. They must write the response to an *IMap* backed *Sink* created using *Sinks.map*. 
3. The request and response types must be serializable and must correspond to the types declared by the corresponding 
   *PipelineDispatcher*.  
4. The map names must follow a certain convention, which is described below.

See *hazelcast.platform.solutions.pipeline.dispatcher.sample.ExamplePipeline* for an example.

> **Note**
> IMaps that are use as service inputs (i.e. IMaps named *\*_request*) must have an event journal configured.  See 
> https://docs.hazelcast.com/hazelcast/5.2/pipelines/stream-imap#step-1-enable-event-journal-in-configuration.



### Map Naming Conventions

By default, the name of the input map is *SERVICE_NAME_request* and the output map is *SERVICE_NAME_response*. For 
example, for a service named *reverse*, the input and output maps would be, respectively, *reverse_request* and 
*reverse_response*.  

However, it is also possible to run multiple versions of a service at the same time and to route traffic between them.
If multi-version support is enabled (see below) then the name of the input map changes to include the version name.
Version names can be any simple string.  For example, if you wish to deploy two versions of the *reverse* service, say 
*v1* and *v2* then the corresponding input map names would be *reverse_v1_request* and *reverse_v2_request*.  

> Note that the output map is not version specific.  both pipelines must write their output to 
> the *reverse_response* map.  

## Configuring Multi-Version Request Routing

If you only want one implementation of a service running at a time, you do not need to configure routing.  Just make 
sure that your service reads the request from the *SERVICE_NAME_request* map and writes the response to the 
*service_name_response* map.  

Multi-version routing is enabled at the service level by loading a routing configuration for the service.
Routing configurations are loaded from JSON formatted files.  A sample configuration for 2 services is shown below. 

```json
{
  "reverse": {
      "versions" : ["v1","v2"],
      "percentages" : [0.5, 1.0]
    },
  "capitalize": {
    "versions" : ["v9","v11"],
    "percentages" : [0.8, 1.0]
  }
}
```
In the example above, 50% of requests to the *reverse* service will go to *v1* and 50% to *v2*.  For the *capitalize* 
service, 80% of requests will go to *v9* and the remaining 20% will go to *v11*.  

The percentages list must be the same length as the versions list and  all percentages must be in the range [0.0,1.0] 
with each one greater than the previous one.  To select the version, the dispatcher generates a random number in 
[0.0, 1.0]. The percentages list is evaluated in order and the version is used that corresponds to the first percentage 
that is greater than or equal to the random number.  For example, for "capitalize" defined above,  a random number of 
.8 would cause "v9" to be used while a random number of .95 would cause "v11" to be used.

A utility to dump and load routing policies is provided. Sample dump and load commands are shown below.  Of course you 
would need to change `N.N.N` to a specific version of the dispatcher.

```bash
# dump
java -cp /opt/project/solution/target/spring-hazelcast-pipeline-dispatcher-N.N.N.jar:/opt/project/solution/target/dependency/* \
    hazelcast.platform.solutions.pipeline.dispatcher.RoutingConfigTool dump \ 
    --hz-cluster-name dev --hz-servers hz-pipeline:5701

#load
java -cp /opt/project/solution/target/spring-hazelcast-pipeline-dispatcher-N.N.N.jar:/opt/project/solution/target/dependency/* \
           hazelcast.platform.solutions.pipeline.dispatcher.RoutingConfigTool load \
           --hz-cluster-name dev --hz-servers hz-pipeline:5701 --input /opt/project/reverse_routing.json 
```

When you load a routing policy, only the services that are in the input file are updated. Services that are not in 
the input file are not changed.  Currently, there is no way to remove the routing policy for a service but you can 
update it to a single version which receives all the traffic.

> **Note** 
> The routing policy automatically takes effect whenever it is updated.  There is no need to restart anything.


# Implementation Details
- This implementation uses an asynchronous architecture for high performance and scalability.  The REST controller's service
method returns a `DeferredResult` and retrieving the response from Hazelcast is also asynchronous.  When the response arrives,
the `setResult` method of the `DeferredResult` instance is called.
- The request is sent to the Pipeline by a `put` on a configurable request map.  The key is client id and a unique request id.
The value is just the request input.
- When the Pipeline has computed the result, it will put the response into a configurable response map.  The key will be the 
same as the key for the originating request.
- The Spring Boot application will use a listener with a predicate, containing its client id, to listen for relevant results.  
- When a result with the matching client id is put into response map, the correct HTTP Server instance will be notified via its listener.
It will then use the unique id to look up the correct `DeferredResult` instance. The result will be sent to the original 
HTTP/REST client by calling the `DeferredResult.setResult` method.

# Release Notes

## 1.1.1
Removed the *hazelcast.pipeline.dispatcher.hazelcast_config_file* property and replaced it with the default Hazelcast 
configuration method as described by these 2 links.
- https://docs.hazelcast.com/hazelcast/5.2/clients/java#configuring-java-client
- https://docs.hazelcast.com/hazelcast/5.2/configuration/understanding-configuration#configuration-precedence

## 1.1
Added dynamic multi-version routing
