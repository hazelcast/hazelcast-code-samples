# Fraud Detection near-cache example

An example demonstrating how a Near Cache configuration option can be added to
an existing application to improve performance.

Performance increases, no coding is required. 

But it's not a universally applicable solution, there are downsides to be aware of.

## What is a Near Cache?

Hazelcast provides a number of distributed storage containers, for storing data in the grid.
The most universal of these are the [IMap](http://docs.hazelcast.org/docs/3.8.5/manual/html-single/index.html#map) and JSR107's [JCache](http://docs.hazelcast.org/docs/3.8.5/manual/html-single/index.html#jcache-api), where the contents are spread across the nodes in the grid. 

If you have ten nodes then each holds 1/10th of the total data content.
This is great for scaling. If data grows by 10%, you can just add an eleventh node and Hazelcast
will rebalance the data across the eleven nodes. Now each has 1/11th of the total data content but crucially each is responsible for about the same amount of data as it was before the growth.

One obvious benefit from the data grid then is capacity. You can hold more data in memory than any one node can cope with, and merely add or remove nodes from the grid to adjust capacity.

The hidden downside is network transfer. If you want an item of data that is stored in the grid, you need to move it across the network from the node on which it is stored to the place where it is needed. This takes time, and the time is influenced by the size of the data and speed of the network.
The network transfer time really should be fast, but if you make lots and lots of calls, it all mounts up.

Normally once you've read such a piece of data you discard it. The "_read_" call results in the data being copied across the network to the local process, then being used, and then dereferenced.

What a `Near Cache adds is a form of local storage.

When you request a data record, it is transferred from the node where it is stored to the process that requested it. What a Near Cache does is *retain* the data record on the process that requested it. If that process makes a second request for that data record, it is already held locally so doesn't have to be retrieved from the node that holds it. So, it is cached near to where it is used.

Note that this doesn't change where the original data record is stored in the grid, what it is doing is making another copy somewhere else.

This is a classic caching pattern. When the _first_ call is made to read the data record, this is a *cache-miss* and the data is retrieved from the node that has it remotely. When the _second_ call is
made to read the data record, this is a *cache-hit* and the data is immediately available. A _third_ call is also a *cache-hit*, as is a _fourth_, and as more and more calls are made the average for the retrieval time reduces.

## When Near Caches are good

* A Near Cache vends a local *copy* of the data to the requestor rather than the master copy.

This gives excellent performance when the same item of data is read repeatedly. The local copy is used rather than the remote copy, network transfer is eliminated, and the retrieval time drops to effectively zero.

* When the data reads are non-uniform

Typically a Near Cache is a subset of the remote data, there is not room to store everything. This gives benefit if some records are read more frequently than others, there is a pattern of localisation. 

Ideally there is a small percentage of data that is frequently used, following some
sort of pareto distribution. For large datasets it isn't realistic for the Near Cache
to hold everything, but excellent performance can be obtained if the subset that is
used most frequently is of moderate size.

## When Near Caches are bad

* A near-cache vends a local *copy* of the data to the requestor rather than the master copy. This may differ from the master copy if the master copy changes -- the Near Cache copy is not immediately updated when the master copy updates.

So the Near Cache data should be viewed as potentially stale. 

This is the trade-off, but may be unacceptable for some applications.

* A Near Cache is essentially an optimization for *reads*, to eliminate network calls. Data *writes* must update the master copy so network calls cannot be avoided.

A Near Cache on data that is mainly being written might give no performance benefit in eliminating network calls. While this might appear to be a neutral use-case, it is actually detrimental as the Near Cache is using up resources on the process that has it.

* Performance with a Near Cache is not uniform or predictable. Items that are in the Near Cache are available instantly and ones that aren't have to be retrieved which incurs a delay.

This then gives varying performance to the calling applications, which may in rare occasions be unacceptable. For example, a first call may miss an SLA and the second
doesn't.

### Why are Near Caches not updated synchronously?

Hazelcast supports multiple clients. Client A might update a data record in the grid that client B, C and D all have copies of in their Near Caches.

To update the Near Cache synchronously would require that clients B, C and D in this example would all have to confirm their Near Caches have been updated before the update initiated by client A could complete. This would significantly slow the response time for client A.

## Configuration

There are numerous configuration options for the Near Cache, described in the Hazelcast documentation. [See here](http://docs.hazelcast.org/docs/latest/manual/html-single/index.html#configuring-near-cache)

## The example - Fraud Detection

When a credit or debit card transaction is made at an ATM, shop or whatever, various checks
take place to approve or decline the transaction. Some are straightforward such as does the
person have sufficient funds, and others are more heuristic in nature to detect suspicious
or criminal activity.

For this example, transactions are limited to those taking place at airports, and the
Bank of Hazelcast is validating these based on geographic location.

The business logic here is that fraudulent activity is confirmed if the time between a person's
transactions is unrealistic for the location of these transactions.

Simply, if you make a transaction in London and three hours later make another transaction in Paris
that is possible. The flying time is under two hours, enough time to make a transaction in London, board a plane, get off and make a transaction in Paris.

If you make another transaction in Johannesburg three hours after that, that is impossible so indicates fraudulent card use. The flying time from Paris to Johannesburg is more than ten hours, so there's no way the real cardholder could make a transaction in those two locations three hours apart.

### Why is a Near Cache relevant?

As we'll see from the example, the Near Cache makes the business logic execute faster.

A higher rate of processing means other checks can also be done in the same time frame, resulting
in a higher percentage of suspicious transactions being identified.

The Near Cache contains the airport locations. There aren't many airports in the world, and obviously their locations won't change, so concerns about stale content aren't relevant.

### Example structure

The example is structured in 5 modules:

```
├── fraud-detection-common/
├── fraud-detection-server/
├── fraud-detection-client-common/
├── fraud-detection-client-without/
├── fraud-detection-client-with/
├── pom.xml
├── README.md
```

#### `fraud-detection-common`

This module defines common objects for the data model, for storing Airports and Users.

#### `fraud-detection-server`

This module defines a Hazelcast server, that has the data model classes on it's classpath
and is built into an executable _Jar_ file by Spring Boot. This makes it easy to run from
the command-line or to deploy to cloud-based containers.

In the `TestData` class are the latitudes and longitudes of 20 airports from around the world.
This data is loaded into the "_airports_" map by the first server in the cluster to start.

#### `fraud-detection-client-common`

This module is shared by the two client modules below.

All the work happens in `fraud-detection-client-common/src/main/java/com/hazelcast/samples/nearcache/frauddetection/FraudService.java`.

What this does is generate a series of 1,000,000 transactions.

For each of these, a user is randomly selected from a small selection of 10 users and an airport is randomly selected from a small selection of 20 airports around the world.

This simulates a credit or debit card transaction, a person uses their card at an airport
and we need to validate this.

Validation is to look for the previous airport they used their card at. We calculate
the distance between these two airports, and decide if this is reasonable or not.

For example, imagine a person last used their card in New York. If three hours later
they attempt to use it in Washington is this reasonable? Well, the distance is
226 miles / 364 kilometers, definitely possibly by plane. So this alone would not
be a reason to reject the transaction.

Now imagine the same person tries to use their card in Zurich three hours after that.
Zurich is 4151 miles / 6681 kilometers, definitely not possibly by plane. So this
is a reason to alert.

That's all there is to the test, how quickly can we process the transactions.

#### `fraud-detection-client-without`

This is a module for a client *without* a Near Cache, packaged by Spring Boot to be
an executable _Jar_ file to run from the command line.

It has a `hazelcast-client.xml` with the minimal details necessary to connect to the cluster.
When it starts it runs the fraud detection test suite located in the `fraud-detection-client-common`
module.

#### `fraud-detection-client-with`

This is a module for a client *with* a Near Cache.

It is almost identical to `fraud-detection-client-without`, except that it's `hazelcast-client.xml` specifies a Near Cache.

These are the lines that are added:

```
<near-cache name="airports">
 <eviction eviction-policy="LFU" max-size-policy="ENTRY_COUNT" size="10"/>
</near-cache>
```

A Near Cache is defined on the "_airports_" map. It keeps the most frequently used 10 entries.

### Running the example

Use `mvn install` to build the example.

The example uses Maven to replace properties in the _fraud-detection-client-common_ module, and uses Spring Boot to build executable Jar files for the clients and
server. These need Maven to run at least as far as the "_package_" phase, but
generally "_install_" is clearer.

#### Randomness

This example uses Java's random number generator, and exploits the algorithm.

The build timestamp from Maven is used as the seed for the random number generator.

Look in `fraud-detection-client-common/target/classes/application.properties` and you should see something
like `build.timestamp=20170928200928`.

What this means is the seed is fixed (unless you recompile). Java's random function,
when primed with a seed, generates a specific sequence of numbers that appear to be
random. If you repeat the seed, you get the same sequence.

This is useful here, as both clients use the build timestamp, so have the same
sequence of transactions generated. This makes exact comparisons possible.

#### Starting the grid

In this example, the servers in the grid do nothing except provide data to the
clients. There is no server side processing.

You need to run at least one server in the grid, and for this example that is
all that is needed.

Start this server using:

```
java -jar fraud-detection-server/target/fraud-detection-server.jar
```

You should get output like the below:

```
2017-09-28 20:59:37.934  INFO 5959 --- [           main] com.hazelcast.cluster.impl.TcpIpJoiner   : [127.0.0.1]:5701 [dev] [3.8] 


Members [1] {
	Member [127.0.0.1]:5701 - 211b3cef-2303-4b50-b9d2-ece1c1bb0881 this
}

2017-09-28 20:59:37.985  INFO 5959 --- [           main] com.hazelcast.core.LifecycleService      : [127.0.0.1]:5701 [dev] [3.8] [127.0.0.1]:5701 is STARTED
2017-09-28 20:59:38.244  INFO 5959 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-09-28 20:59:38.340  INFO 5959 --- [           main] c.h.i.p.impl.PartitionStateManager       : [127.0.0.1]:5701 [dev] [3.8] Initializing cluster partition table arrangement...
2017-09-28 20:59:38.425  INFO 5959 --- [           main] c.h.s.n.frauddetection.MyInitializer     : Loaded 20 into 'airports'
2017-09-28 20:59:38.429  INFO 5959 --- [           main] c.h.s.n.frauddetection.Application       : Started Application in 4.726 seconds (JVM running for 5.294)
```

In the second last line above, `Loaded 20 into 'airports'` confirms that test data has been loaded for
the airports.

If you want, you can start more servers using the same command. They should select different points and join together.

When the first server starts, it populates itself with test data for airports in the "airports_" map. If a second, third or more servers are started, they will not repeat the data load.

#### Starting a client without near-caching

Once the grid is up and running, use this command to run the client without Near Cache:

```
java -jar fraud-detection-client-without/target/fraud-detection-client-without.jar
```

The client will connect to the grid, apply a series of "financial transactions" and then
shut down.

Add it runs, it will log out the first few suspicious transactions it spots, and this might
look like:

```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~           A L E R T           ~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~ User : '8'
~~~ Only three hours between card used at
~~~ Paris Charles De Gaulle
~~~ Washington Dulles
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~           A L E R T           ~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~ User : '7'
~~~ Only three hours between card used at
~~~ Madrid
~~~ New York John F Kennedy
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
```

The transactions are randomly generated, so you will likely see different user ids and airports.
For example, only three hours gap between using a credit or debit card in Madrid and New York
is pretty much impossible for civil aircraft.

When it shuts down, it will report final tally figures for how it performed.

These could look like this:

```
===================================== 
===         R E S U L T S         === 
===================================== 
=== Map : 'airports'
===  Calls............. : '1899542'
===  Alerts............ : '390273'
===================================== 
===  Run time for tests : 'PT5M55.963S'
===================================== 
2017-09-28 21:10:13.851  INFO 6118 --- [           main] c.h.s.n.frauddetection.Application       : Started Application in 358.551 seconds (JVM running for 359.113)
```

The exact figures depend obviously on the strength of the machine you run this on. Think of them more as a baseline to compare against the next step.

In this example 1,899,542 calls were made from the client to the server for the airports (as there is no near cache). Total run time was 358 seconds, so almost exactly 6 minutes.

#### Starting a client with near-caching

Once the grid is up and running, use this command to run the client with a Near Cache:

```
java -jar fraud-detection-client-with/target/fraud-detection-client-with.jar
```

This client will connect to the grid, run the exact same processing as the previous
client, and produce it's final performance results. 

Again, alerts are generated the first few are logged to the screen:

```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~           A L E R T           ~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~ User : '8'
~~~ Only three hours between card used at
~~~ Paris Charles De Gaulle
~~~ Washington Dulles
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~           A L E R T           ~~~ 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
~~~ User : '7'
~~~ Only three hours between card used at
~~~ Madrid
~~~ New York John F Kennedy
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
```

Note here this is the same series of alerts. The '_random_' series of transactions uses the
same seed remember for both clients, so generates the same transactions for each build. 

As this client has a Near Cache, we are expecting a beneficial effect on performance.
The final results prove it.

```
===================================== 
===         R E S U L T S         === 
===================================== 
=== Map : 'airports'
===  Calls............. : '1899542'
===  Alerts............ : '390273'
===  Near Cache hits... : '989393'
===  Near Cache misses. : '910149'
===================================== 
===  Run time for tests : 'PT4M20.652S'
===================================== 
2017-09-28 21:04:08.765  INFO 5960 --- [           main] c.h.s.n.frauddetection.Application       : Started Application in 263.292 seconds (JVM running for 264.269)
```

Again, 188,952 calls were made, but 989,393 of these were satisfied by the Near Cache.
The Near Cache in the `hazelcast-client.xml` for this process is sized at 10, and the test data
has 20 airports. Given we are randomly selecting with a uniform distribution amongst airports,
this is as you might expect.

Run time is 264 seconds, 4.5 minutes, quite a reduction from 6 minutes. Although we have halfed
the calls to the server for airports, we still have to call the server for users, so run time
overall does not half.

Try varying the parameters of the Near Cache (in the `hazecast-client.xml` file) to see how the performance varies.

Increasing the size of the Near Cache will always improve the hit rate, so this is the
wrong statistic to focus on for size. What matters is the throughtput, how many requests
are serviced.

If the overall performance improves, then the Near Cache was too small.
If the overall performance degrades, then the Near Cache was too big -- it's size is causing the JVM to run garbage collection work alongside the Hazelcast 
client application, meaning that the Hazelcast client application gets a lesser
share of the JVM resources and so achieves less.

## Other considerations

### Eviction and expiry

For best responses, the Near Cache should hold every data record in the underlying data
store, rather than just a subset.

However, this is only practical for small data sets. In all other cases, the
_eviction_ and _expiry_ configuration options need to be used to constrain
the memory usage of the Near Cache.

### Querying

Queries do not use the Near Cache. The Near Cache may in general only contain a subset of
of the data so would give incorrect results to query, if all items had not yet been
loaded or insufficient space to hold all items existed.

### Server-side

In this example, the Near Cache is on a client-side process, that connects to the Hazelcast
data grid but is not responsible itself for hosting any data.

Near Caches can also be used on server-side processes, the nodes in the data grid. Such a
server process would then be both responsible for hosting it's share of the data and also
the Near Cache copy of data that process was actually using.

Apart from a few optimizations, the concept is the same -- the Near Cache is copy of data
that is elsewhere. The difference is in memory usage, the process already has data in
memory apart from the Near Cache, so needs careful sizing to ensure there is enough for
both.

## Summary

A `Near Cache` is a second level cache, a local copy of data held in the main Hazelcast grid.
The main Hazelcast grid is the first level cache, perhaps of data that resides in
a relational database on disk. So a Near Cache can be viewed as a cache of a cache.

Anything where a local copy exists accelerates read calls, as network transmission
times are eliminated.

Anything where a local copy exists means a temporary mismatch when the remote copy
changes. A "_stale_" read has to be tolerable to the application until the local copy
is refreshed.

A Near Cache is easy configuration to add after development, and to tune to get the best
hit-ratio.

In this example, run-time was reduced by 25%. Exact results will vary due to factors like
machine speed, network speed and so on. Nevertheless, a 25% gain for 3 lines in a config file is highly desirable.
