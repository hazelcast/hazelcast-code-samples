Contains example for Generic Record and GenericRecordBuilder API

These two API will enable accessing the data without having user domain classes
on the server. Main use cases are EntryProcessor and any InstanceAware callable.

With this new feature, a new business logic could be introduced to cluster without
needing to restart the cluster. 

To run the example, first start an empty Cluster
`
cd generic-record-cluster
mvn compile exec:java -Dexec.mainClass="example.Cluster" 
`

Then start one of the client examples with new domain classes above:

# CallableExample
In this example, how GenericRecord can be used together with an instance aware Callable is shown.
Note that our cluster has no config for PortableFactory. When a factory config is missing, an instance(client/member)
returns GenericRecord instead of user provided domain object.  
`
cd generic-record-client
mvn compile exec:java -Dexec.mainClass="example.CallableExample" 
`
# EntryProcessorExample  
In this example, how GenericRecord can be used together with an EntryProcessor is shown. 

`
cd generic-record-client
mvn compile exec:java -Dexec.mainClass="example.EntryProcessorExample" 
`

# ClientWithoutDomainObjectExample
In this example, a client is populated with GenericRecord instead of user provided domain objects and queried.
`
cd generic-record-client
mvn compile exec:java -Dexec.mainClass="example.ClientWithoutDomainObjectExample" 
`
