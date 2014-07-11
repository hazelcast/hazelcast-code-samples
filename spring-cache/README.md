Hazelcast-SpringCacheManager-Example
====================================

This is a sample application which uses Hazelcast as Spring Cache Provider. 
This setup is configured to run in Client/Server Mode where Hazelcast Cache Manager connects to an existing Hazelcast instance. 


Run Sample App
==============

**Declarative Configuration Example**

mvn compile exec:java -Dexec.mainClass="com.hazelcast.spring.cache.DeclarativeCacheManager"

**Annotation Based Example**

mvn compile exec:java -Dexec.mainClass="com.hazelcast.spring.cache.AnnotationBasedCacheManager"
