# Hibernate 2nd Level Cache with Hazelcast

In this repository, you can find a sample implementation of hibernate 2nd level cache with hazelcast. You can also find detailed explanation at http://hazelcast.org/ 

## Prerequisites

You should have installed Apache Maven and Mysql database.

`hibernate-core`, `hazelcast` and `hazelcast-hibernate53`dependencies exist in pom.xml.

Before running the application, make sure you have a valid mysql user (root by default). If you do not use a password
for that user, remove this property in HibernateUtil.java:

```
setProperty("hibernate.connection.password", DB_PASSWORD)
```

## What Application does

The app simply creates 3 sessions each of which runs sequentially and is closed before the next one is opened.   

`Session1:` Creates 10 entries and saves on database. The secondary cache contains all the entries when the session is closed.   
`Session2:` Gets the 10 entries inserted in session1. No databases hits performed since all the values exist in the secondary cache.
Hence no Sql commands must be seen in the log.    
`Session3:` Before this session starts, the cache is evicted. Hence all the queries must hit the database
and secondary cache miss count must be 10.

## How to Run Sample Application

* Compile project:

```
mvn compile
```

* Run the project:

When you run the project, it will create a database on your MySQL server. You can configure the database  
via execution arguments. If you do not use args, then the default values `DB_USERNAME(root)`, `DB_PASSWORD(root)` and 
`DB_NAME(hazelcast_demo_db)` will be used. The database will be dropped when the application stops.

```
 mvn exec:java -Dexec.mainClass=com.hazelcast.hibernate.App -Dexec.args="<username> <password> <db_name>"
```


## Extras

- Currently the app uses the default configuration for the map. You can change this default configuration
on `hazelcast.xml`:

```
<map name="default">
    <in-memory-format>BINARY</in-memory-format>
    <backup-count>1</backup-count>
    <read-backup-data>false</read-backup-data>
    <async-backup-count>0</async-backup-count>
    <time-to-live-seconds>0</time-to-live-seconds>
    <max-idle-seconds>0</max-idle-seconds>
    <eviction-policy>NONE</eviction-policy>
    <max-size policy="PER_NODE">0</max-size>
    <merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
</map>
```

Also, if you want to change the configuration for a particular entry, add another map
configuration. Map name can be changed to a custom value on `TestObject.class` inside `@Cache`annotation:

```
<map name="com.hazelcast.hibernate.entity.TestObject">
    <in-memory-format>BINARY</in-memory-format>
    <backup-count>1</backup-count>
    <read-backup-data>false</read-backup-data>
    <async-backup-count>0</async-backup-count>
    <time-to-live-seconds>100</time-to-live-seconds>
    <max-idle-seconds>100</max-idle-seconds>
    <eviction-policy>NONE</eviction-policy>
    <max-size policy="PER_NODE">0</max-size>
    <merge-policy>com.hazelcast.map.merge.PassThroughMergePolicy</merge-policy>
</map>
```


 
