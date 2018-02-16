# Times Table with JCache 1.1

# An example showing Hazelcast support for _JCache 1.1_.

As a maintenance release to the JSR 107 Java Caching Specification, _JCache 1.1_ was released on 16th December 2017.

This is now supported by Hazelcast, from release 3.9.3 onwards, released on 16th February 2018.

This example allows you to build either version, to compare the differences. Mainly the differences are quite
minor, but _JCache 1.1_ is useful if you wish to mix Spring style caching with standard Java caching.

# Caching

One definition of cache is a copy of something that is elsewhere, but that is then faster to access.

This phrasing outlines two significant aspects.

Firstly, the performance boost comes from time not distance. It's generally assumed that the cache
will be physically nearer than the original, but it can be further away so long as you get to it
quickly enough.

Hazelcast provides both options here. It can be an in-process cache so is geographically adjacent
to the application. More usually it is a multi-process cache so most of the data will be slightly
further away, a tolerable trade-off to give increased capacity, scalabilty and resilience.

Secondly, the re-use. Once you have the readily available copy, it only provides any benefit for the
next access. For a cache that is empty to begin with, you have to read an item more than once to
benefit.

## Data Caching

The most frequent use-case is data caching.

A data record is retrieved from somewhere, such as a relational database based on disk technology,
and once retrieved the retrieved copy is retained in memory.

If that data record is requested again, and is found in the cache, it can be returned from the
cache without needing to interact with the relational database again.

## Method Caching

The counterpart use-cache is method caching.

Here what is retrieved is the result of a calculation, for example the square of a number,
and once calculated the square is retained in memory.

If the square of the number is requested again, and the result of the calculation is in the cache,
the calculation can be bypassed.

## Do these differ ?

No.

In the data caching use-case the input is the key of the data record, there is a function where
there is a reformatting (using an ORM such as Hibernate), and it is the result that is cached.

In the method caching use-case the input are the arguments to a function, the function runs,
and the result is produced and cached.

The different really is conceptual. in the former the input and the output perceived as
different representations of the same logical item. In the latter the input leads to the
output, rather than resembles the output.

# JCache

JCache is the Java Caching standard, also known as JSR107 (_Java Specification Request_ number 107).

The key point of course is standardisation.

The Java Caching standard specifies API calls. So _your_ code can make a call such as `getCache("apple")`
and receive a reference to an object that behaves in a predefined manner.

The flip side is that something has to provide this behaviour. Hazelcast provides an implementation
of JCache, validated by the JCache test suite.

This means your code can interact with the JCache API, get the behaviour you expect, and you not
really be aware that Hazelcast is part of the tech stack.

You will probably be aware that Hazelcast is in the mix. You've set project dependencies,
there are log messages, and you'll be getting excellent performance, scalability and
resilience. But the point remains that you don't need to be aware.

Other JCache implementation providers exist, if you'd prefer something slower, but they're
still going to *have* to behave in the same way.

And of course, the JCache standard can itself evolve. So the original version 1.0 from March 2014
has had a maintenance release 1.0 issued in December 2017 with some enhancements.

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

Specifically, the application calculates _Times Tables_. "*Fives times six is thirty*", "*six times six is thirty-six*", and so on.

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

## JCache 1.1 - server

Once everything is compiled, we can launch one or more instances of Hazelcast IMDG.

Use the command:

```
java -jar server/target/server-0.1-SNAPSHOT.jar
```

Once this starts, you should see a command line prompt indicating that we have "*jsr107=1.1.0 >*" as the
prompt, confirming we have built as JCache 1.1.

In this example, the IMDG server is just a common place to store the caches. If you like, there
are two useful commands, _clients_ to list the currently connected clients, and _list_ to show
which caches current exist.

## JCache 1.1 - plain Java client

Once one or more servers are running. use this command to start a Hazelcast JCache client.

```
java -jar jcache-standard/target/jcache-standard-0.1-SNAPSHOT.jar
```

One this has started, issue the command "*times 5 6*" to request that 5 times 6 is calculated.
Behind the scenes, this is using `CacheManager.getCache(String cacheName)` to find the cache.

As this is the first time the command is run, all caches are empty. So 5 times 6 has to be
calculated, as does 4 times 6, 3 times 6, 2 times and 1 times 6.

Each of these calculations produces a result that is stored in the cache. As the code
has cache listeners, you should should see logging indicating the creating of cache
entries. It might look like the below:

```
jsr107=1.1.0 > times 5 6
> TIMES
21:10:50.265 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
21:10:50.268 INFO  main c.h.samples.jcache.timestable.CLI - Retrieve 5 * 6 
21:10:50.273 INFO  main c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=5, operand2=6)) 
21:10:50.309 INFO  main c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=4, operand2=6)) 
21:10:50.310 INFO  main c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=3, operand2=6)) 
21:10:50.313 INFO  main c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=2, operand2=6)) 
21:10:50.315 INFO  main c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=1, operand2=6)) 
21:10:50.339 INFO  main c.h.samples.jcache.timestable.CLI - Result 30 
21:10:50.340 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
21:10:50.346 INFO  main c.h.samples.jcache.timestable.CLI - Elapsed PT0.067S 
21:10:50.346 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
===================================================
[CACHEMANAGER, CACHENAMES, QUIT, TIMES, TIMESTABLE]
===================================================
jsr107=1.1.0 > 21:10:50.347 INFO  _hzinstance_jcache_shared.event-1 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=1, operand2=6)' V=='6' 
21:10:50.347 INFO  _hzinstance_jcache_shared.event-5 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=4, operand2=6)' V=='24' 
21:10:50.347 INFO  _hzinstance_jcache_shared.event-5 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=5, operand2=6)' V=='30' 
21:10:50.347 INFO  _hzinstance_jcache_shared.event-4 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=3, operand2=6)' V=='18' 
21:10:50.347 INFO  _hzinstance_jcache_shared.event-1 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=2, operand2=6)' V=='12' 
```

As further proof, then try the "*timestable*" command to see what is stored in the timestable cache.
You should see something like this:

```
jsr107=1.1.0 > timestable 
> TIMESTABLE
21:14:14.961 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
21:14:15.175 INFO  main c.h.samples.jcache.timestable.CLI -  => 'Tuple(operand1=1, operand2=6)' == '6' 
21:14:15.176 INFO  main c.h.samples.jcache.timestable.CLI -  => 'Tuple(operand1=2, operand2=6)' == '12' 
21:14:15.176 INFO  main c.h.samples.jcache.timestable.CLI -  => 'Tuple(operand1=3, operand2=6)' == '18' 
21:14:15.176 INFO  main c.h.samples.jcache.timestable.CLI -  => 'Tuple(operand1=4, operand2=6)' == '24' 
21:14:15.177 INFO  main c.h.samples.jcache.timestable.CLI -  => 'Tuple(operand1=5, operand2=6)' == '30' 
21:14:15.177 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
21:14:15.177 INFO  main c.h.samples.jcache.timestable.CLI - [5 cache entries] 
21:14:15.177 INFO  main c.h.samples.jcache.timestable.CLI - ----------------------- 
===================================================
[CACHEMANAGER, CACHENAMES, QUIT, TIMES, TIMESTABLE]
===================================================
jsr107=1.1.0 >
```

The key here is a tuple (a pair). If the tuple is 4 and 6, the corresponding stored value is 24. So,
4 time 6 is 24. Cached computation!

## JCache 1.1 - Spring client

Now we shall try the same with a client using the [Spring Framework](https://spring.io/). Spring is
different but very popular way to develop Java commands.

Start this client with

```
java -jar jcache-spring/target/jcache-spring-0.1-SNAPSHOT.jar
```

This is intended to be similar to the standard Java style Hazelcast client. The difference is that
Spring deduces that cache key and values type for the "*timestable*" cache.

Here, we want to know what 6 times 6 is. The syntax to enter is `times --x 6 --y 6`.

You will get log output here like this:

```
21:25:01.447 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - ----------------------- 
21:25:01.452 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - Retrieve 6 * 6 
21:25:01.501 INFO  Spring Shell c.h.s.jcache.timestable.BusinessLogic - product(Tuple(operand1=6, operand2=6)) 
21:25:01.509 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - Result 36 
21:25:01.510 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - ----------------------- 
21:25:01.514 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - Elapsed PT0.052S 
21:25:01.514 INFO  Spring Shell c.h.samples.jcache.timestable.CLI - ----------------------- 
21:25:01.514 INFO  _hzinstance_jcache_shared.event-2 c.h.s.jcache.timestable.MyCacheListener - Cache entry CREATED K=='Tuple(operand1=6, operand2=6)' V=='36' 
```

The key thing to note is only one cache entry is created (for 6 times 6). The cache entries for 5 times 6, 4 times 6 etc already exist and are used.

So essentially, the newly started Spring client is using cached values created by the Java client. 

This is what we'd want.

A standard Java Hazelcast client can use cache values already created by a Spring Hazelcast client, and vice
versa.

# JCache 1.0

Next we try with JCache 1.0. Ensure all JVMs from the previous JCache 1.1 run are stopped, and then compile with:

```
mvn -Pjcache10 clean install
```

This time the parameter "_-Pjcache10_" selects a different build profile, with different software versions, to get
the previous behaviour.

Again, the "_clean_" option deletes previous compilations, which may have been for JCache 1.1 not 1.0.

And again, the command prompt adjusts to display the version in use.

## JCache 1.0 - server

As for the JCache 1.1, for the JCache 1.1 run through, use this command to start one or more Hazelcast
servers:

```
java -jar server/target/server-0.1-SNAPSHOT.jar
```

All that's different here is the server should give a different prompt to confirm it is running
with the JCache 1.0 specification instead of JCache 1.1

## JCache 1.0 - client

Now try either a plain Java Hazelcast client or a Spring Java Hazelcast client.

For the standard Java client

```
java -jar jcache-standard/target/jcache-standard-0.1-SNAPSHOT.jar 
```

Or for the Spring Java client

```
java -jar jcache-spring/target/jcache-spring-0.1-SNAPSHOT.jar
```

Both of these will start. However, if either tries to run a times table calculation it will fail.

If you try `times 5 6` on the Java version or `times --x 5 -- y 6` on the Spring version, you will
get the `IllegalArgumentException` that the JCache 1.0 specification requires.

Something like

```
java.lang.IllegalArgumentException: Cache timestable was defined with specific types Cache<class com.hazelcast.samples.jcache.timestable.Tuple, class java.lang.Integer> in which case CacheManager.getCache(String, Class, Class) must be used
	at com.hazelcast.cache.impl.AbstractHazelcastCacheManager.getCache(AbstractHazelcastCacheManager.java:227)
	at com.hazelcast.cache.impl.AbstractHazelcastCacheManager.getCache(AbstractHazelcastCacheManager.java:67)
	at com.hazelcast.samples.jcache.timestable.CLI.times(CLI.java:203)
	at com.hazelcast.samples.jcache.timestable.CLI.process(CLI.java:69)
	at com.hazelcast.samples.jcache.timestable.Application.main(Application.java:44)
```

This is, of course, because the code is unchanged. Both clients are directly or indirectly calling
the `CacheManager.getCache(String cacheName)` method, which will (in JCache 1.0) throw an exception if the
cache has been configured with key and value types.

### Note - Not Impossible

Using the same cache with standard Java and Spring Java is not impossible.

Using the same cache, with defined key and value types, with standard Java and Spring Java is not impossible.

It's just a lot simpler with JCache 1.1 than JCache 1.0.

That's why it's been used here. JCache 1.1 is a maintanence release -- it makes JCache better but isn't
a major change to funcationality.

# Summary

JCache 1.1 is a maintenance release of the JCache specification. Although not as significant a milestone
as the launch of JCache itself (1.0), it does make a few useful amendments.

JCache 1.1 is implemented by Hazelcast 3.9.3 and beyond.

The change to `CacheManager.getCache(String cacheName)` in JCache 1.1 makes it much easier to mix Spring
use of JCache and standard Java use of JCache.
