# Implementing Tenant Control for JCache

## Introduction

`TenantControl` SPI is a mechanism that allows decorating JCache operations on Hazelcast members, so that some context setup is executed before the actual operation is ran and tear down is executed after operation execution.

A `TenantControl` is constructed by a `TenantControlFactory` which is configured on Hazelcast members with a server provider configuration file in `META-INF/services/com.hazelcast.spi.tenantcontrol.TenantControlFactory`.

## About this sample

This sample implements rudimentary classpath separation per cache by configuring Hazelcast with a `MultiTenantClassLoader` and configuring a `TenantControl` implementation that sets up the thread context class loader, so JCache operations are executed with the context classloader corresponding to the cache in use. Under this scheme, cache "apps.app1" is allowed access to class in package `apps.app1`, cache "apps.app2" accesses package `apps.app2` etc.

## Building and running the sample

Build with `mvn clean install`, then bootstrap the Hazelcast member:

```
$ mvn -Pmember exec:java
```

and execute the Hazelcast client with:

```
$ mvn -Pclient exec:java
```


The client is expected to fail invoking the `IllegalPersonProcessor` on cache `apps.app2`, as the classloader for that cache will deny access to class `apps.app1.Person`. Sample output:

```
app1 cache value for key 1 is Person{id=1, name='Evan'}
app1 cache value for key 2 is Person{id=2, name='Sammy'}
javax.cache.processor.EntryProcessorException: com.hazelcast.client.UndefinedErrorCodeException: Class name: java.lang.NoClassDefFoundError, Message: apps/app1/Person
        at com.hazelcast.client.cache.impl.ClientCacheProxy.invokeInternal(ClientCacheProxy.java:337)
        at com.hazelcast.client.cache.impl.ClientCacheProxy.invoke(ClientCacheProxy.java:311)
        at multitenant.BootstrapClient.main(BootstrapClient.java:25)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
        at java.lang.Thread.run(Thread.java:748)
Caused by: com.hazelcast.client.UndefinedErrorCodeException: Class name: java.lang.NoClassDefFoundError, Message: apps/app1/Person
...
...
app2 cache value for key 3 is null
```

