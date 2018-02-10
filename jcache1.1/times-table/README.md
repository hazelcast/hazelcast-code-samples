# Times Table with JCache

# TODO - XXX - Date of 3.9.3 release - 13th ??

# An example showing Hazelcast support for _JCache 1.1_.

As a maintenance release to the JSR 107 Java Caching Specification, _JCache 1.1_ was released on 16th December 2017.

This is now supported by Hazelcast, from release 3.9.3 onwards, released on ??th February 2018.

This example allows you to build either version, to compare the differences. Mainly the differences are quite
minor, but _JCache 1.1_ is useful if you wish to mix Spring style caching with standard Java caching.

## JCache 1.1 differences

The JCache 1.1 specification [JSR107 v1.1.0](https://github.com/jsr107/jsr107spec) lists all the changes from JCache 1.0.

### `CacheManager.getCache(String cacheName)`

In this example, the one we will focus on is `CacheManager.getCache(String cacheName)`

The JCache 1.1 definition is [here](https://static.javadoc.io/javax.cache/cache-api/1.1.0/javax/cache/CacheManager.html#getCache-java.lang.String-).
The JCache 1.0 definition is [here](https://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/CacheManager.html#getCache\(java.lang.String\)).

The difference between two, is JCache 1.0 can throw `IllegalArgumentException` and JCache 1.1 can't,
and is used to lookup a cache by name. In JCache 1.0, if you don't know the key/value classes
for the cache this method can't be used, and in JCache 1.1 it can.

Note in [the interface](https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/CacheManager.java#L221) the exception is not specified.

## Build configuration

To use _JCache 1.1_ you need to Hazelcast IMDG version 3.9.3 and later. The Maven dependencies would be as below:

```
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast</artifactId>
	<version>3.9.3</version>
</dependency>
<dependency>
	<groupId>javax.cache</groupId>
	<artifactId>cache-api</artifactId>
	<version>1.1.0</version>	
</dependency>
```

For _JCache 1.0_ the previous and only other release version, use Hazelcast IMDG 3.9.2 or earlier. The Maven dependencies would be as below:

```
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast</artifactId>
	<version>3.9.2</version>
</dependency>
<dependency>
	<groupId>javax.cache</groupId>
	<artifactId>cache-api</artifactId>
	<version>1.0.0</version>	
</dependency>
```

*NOTE* You should not try to mix the versions. Hazelcast is unable to determine which version of JCache is on the
classpath. Hazelcast 3.9.3 gives the new behaviour so must have JCache 1.1 classes. Hazelcast 3.9.2 has the old
behaviour so must have JCache 1.0 classes.

The `pom.xml` in this example includes two build profiles, one for each style. So compile with `mvn -Pjcache11 clean install` for the new behaviour and `mvn -Pjcache10 clean install` for the old.

# The application: Times Table

The application here is the trivial, as the objective here is to show how the clients can interact with cached data.

Specifically, the application calculates _Times Tables_. "*Fives times six is thirty*", "*six times time is thirty-six*", and so on.

However, rather than do this the normal way by multiplication, this is calculated by iterative addition.

In other words "*six times six*" is calculated as "*six plus (five times six)*". So there is a recursive chain
of calls, and "*six times six*" becomes "*six plus (six plus (six plus (six plus (six plus (six)))))*".

This is where the caching comes in. If we have already calculated "*five times six*" _and_ cached the result,
then the calculation for "*six times six*" can add "*six*" to the cached result for "*five times six*".

# JCache 1.1

The first thing we will do is explore JCache 1.1 behaviour.

To do this, we must first compile the example with the build profile "_jcache11_".

This will do this using Maven:

```
mvn -Pjcache11 clean install
```

The parameter "_-Pjcache11_" instructs Maven to use the variable definitions for this build profile, as there
are two.

The stage "_clean_" is pretty important too. This instructs Maven to first delete any previous build, so we
can be sure we are not picking up code compiled with a different build profile.

As a double check, all the interactive modules have the version of JCache in the prompt.

# JCache 1.0

Next we try with JCache 1.0. Compile with:

```
mvn -Pjcache10 clean install
```

This time the parameter "_-Pjcache10_" selects a different build profile, with different software versions, to get
the previous behaviour.

Again, the "_clean_" option deletes previous compilations, which may have been for JCache 1.1 not 1.0.

And again, the command prompt adjusts to display the version in use.

# Summary

JCache 1.1 is a maintenance release of the JCache specification. Although not as significant a milestone
as the launch of JCache itself (1.0), it does make a few useful amendments.

JCache 1.1 is implemented by Hazelcast 3.9.3 and beyond.
