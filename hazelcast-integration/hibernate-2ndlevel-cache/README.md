# Hibernate 2nd Level Cache with Hazelcast

In this repository, you can find a sample usage of Hibernate 2nd level cache with Hazelcast. You can also find 
detailed explanation about the plugin at the [repository](https://github.com/hazelcast/hazelcast-hibernate). 

## Prerequisites

You need [Apache Maven](http://maven.apache.org/download.cgi) installed.


## How to Run

- Compile project using:

```
mvn compile
```

- There are three different classes demonstrating entity, collection and query caches. In each of them, 
the behavior is explained in comments and the second level cache statistics are printed after each interaction.
To run them:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.EntityCache"
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CollectionCache"
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.QueryCache"
```

- Hibernate and Hazelcast configuration can be set on xml files under `resources` directory. For instance,
if you want to use the L2C with Hazelcast client, then you need to set the corresponding property in    
`hibernate.cfg.xml` such that:
```
<property name="hibernate.cache.hazelcast.use_native_client">false</property>
```

Keep in mind that if Hazelcast client is used, then there must be a Hazelcast instance up and running. The
execution of the command below will create a Hazelcast instance:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.StartInstance"
```
Running this command multiple times will yield Hazelcast instances to connect each other and form a cluster and 
hence storing the cached data distributed. 
