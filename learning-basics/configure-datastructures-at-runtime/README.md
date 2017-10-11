Configuring Data Structures at Runtime
======================================

Since Hazelcast IMDG 3.9, it is possible to add data structure configuration at runtime. This sample project demonstrates how to add new configuration for `IMap` data structure at runtime and use it from member- and client-side `HazelcastInstance`s.  

Compiling
=========
If you have already built the parent maven project, then there is nothing special to do. If you want to compile only this module, make sure `com.hazelcast.samples::helper` artifact is already installed in your local maven repository. The `helper` maven module is located in `hazelcast-code-samples/helper`.  


Build and Run Sample Apps
=========================

**Member-side Dynamic Config Example**

The java source for this example is class `MemberDynamicConfig`. Build and execute with:

```
mvn -Pmember package exec:java
```

**Client-side Dynamic Config Example**

The java source for this example is class `ClientDynamicConfig`. Build and execute with:

```
mvn -Pclient package exec:java
```
