# YAML Configuration

The samples in this module demonstrate how to configure a Hazelcast cluster with YAML configuration.
The YAML configuration feature requires Java runtime version 8 or higher.  

### Configuration Lookup

Creating member instance with `Hazelcast.newHazelcastInstance()` looks for configuration files
in multiple locations both in XML and YAML format. XML configuration takes precedence
over YAML configuration in all locations resulting the following lookup order:
1. The configuration passed in `hazelcast.config` JVM argument, either XML or YAML
2. `hazelcast.xml` in the working directory
3. `hazelcast.xml` on the classpath
4. `hazelcast.yaml` in the working directory
5. `hazelcast.yaml` on the classpath
6. default configuration `hazelcast-default.xml`

Similarly, the (Java) client instances created with `HazelcastClient.newHazelcastClient()` follows
the same lookup logic and order:
1. The configuration passed in `hazelcast.client.config` JVM argument, either XML or YAML
2. `hazelcast-client.xml` in the working directory
3. `hazelcast-client.xml` on the classpath
4. `hazelcast-client.yaml` in the working directory
5. `hazelcast-client.yaml` on the classpath
6. default configuration `hazelcast-client-default.xml`

The sample `ConfigLookup` demonstrates how the configuration files are located on the classpath and 
how to use the locator logic to take only YAML configuration files into account. The member and client 
YAML configuration files in this sample import network configuration for composing configuration from 
multiple YAML files.  

### Loading YAML Configuration Without Lookup

If it is known that the cluster will be configured with YAML configuration and no need for using the location 
logic shown in the first sample, the following YAML-specific `Config` classes can be used:
- `ClasspathYamlConfig`: loads the configuration from the classpath based on the provided filename
- `FileSystemYamlConfig`: loads the configuration from the file system based on the provided filename
- `UrlYamlConfig`: loads the configuration from the provided URL
- `InMemoryYamlConfig`: loads the configuration from the provided `String` containing YAML configuration 

In the case of Java clients `ClientClasspathYamlConfig` can be used for the same purpose.
  
The sample `YamlConfigClasspath` loads the member configuration `hazelcast-sample.yaml` from the classpath and 
demonstrates variable replacement in YAML configuration by defining the member's port in variable taken from 
the system properties and then from a provided `Properties` instance.   

#### Full YAML Configuration Example

There is a full example YAML configuration in the distributed Hazelcast jars, named `hazelcast-full-example.yaml`.
In the lack of schema definitions and hence lack of auto-completion in IDEs this example file can be used
for reference when writing YAML configuration.
