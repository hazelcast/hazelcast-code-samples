# Priority queueing using the SPI

Hazelcast provides distributed queues, an implementation of `java.util.concurrent.BlockingQueue`.

However, an implementation of `java.util.concurrent.PriorityBlockingQueue` is not yet provided.

In this example, we'll see how to write this yourself, using Hazelcast's _SPI_
(Service Provider Interface).

(*Note*: this is just an example, deliberately simplified. Attention is drawn to
the some extra steps to make this production quality.)

## Recap, "*SPI*"

Note that an *SPI* is not an *API*. The *SPI* essentially allows your
objects to run _inside_ Hazelcast alongside the pre-defined distributed objects
such as `IMap` and `IQueue`, rather than as a layer on top that an *API* defines.

While this is better in many ways, it does require a bit more understanding
of how Hazelcast works behind the scenes.

You can make your objects behave like standard Hazelcast objects -- 
distributed, partitioned, remotely-accessible, all the good stuff.
But you do have to understand some of the details.
 
## Recap, `IQueue`

Hazelcast's `com.hazelcast.core.IQueue` is an implementation of `java.util.concurrent.BlockingQueue`
that runs on the IMDG grid.

What this means in practice is that one thread can write data to the queue,
and another thread can read from the queue. Because this is Hazelcast these
threads can be in the same JVM, in different JVMs, on different hosts, and
it all works the same.

Plus, behind the scenes the queue has configurable capacity, resilience and
persistence, so it's as robust as you want it to be, protected from
failures and preservable across a restart.

But it is a strict queue, items are read in the order they are written.
There is no notion of ordering or requirement that they be `Comparable`.

## The problem

So, the problem here is add prioritisation.

Make a distributed object than one thread can write to and another
thread (on a different JVM, etc) can read from, but now to read the
most important item first.

## The sample solution

The example consists of two modules:

```
spi-priority-queue-core
spi-priority-queue-server
```

The core module (`spi-priority-queue-core`) defines our priority queue, the necessary bits to
plumb it into Hazelcast and our data model.

The server module (`spi-priority-queue-server`) defines a test framework to demonstrate
the example.

### The data model


### TODO


## Running the sample solution

These are Spring Boot examples, so run `mvn` as far as the "_package_" stage.

Something such as `mvn install` from the top level will build an executable
jar file for the server, bundling in the common module.

To start a cluster with one Hazelcast server instance, run this command:

```
java -jar spi-priority-queue-server/target/spi-priority-queue-server-0.1-SNAPSHOT.jar
```

If you can, run the same command in more windows concurrently to create
multiple servers which should join together to form a cluster.

## What the sample solution is missing

To keep the sample simple, some parts have been omitted.

`MigrationAwareService` is not yet implemented, but would not be that difficult to
add. This would allow queue contents to be moved from server from server
when the cluster changes size (servers are added or removed) and the
data load needs rebalanced. This would be a necessary step for a
production strength implementation.

Client access is not yet implemented, but would be a more serious coding
effort. In Hazelcast, the client-server protocol is public -- see
(here)[https://github.com/hazelcast/hazelcast-client-protocol/raw/v1.2.0/docs/published/protocol/1.2.0/HazelcastOpenBinaryClientProtocol-1.2.0.pdf].
What this means in practice is client-server access needs to adhere
to this protocol, which means the use of _codecs_, and beyond the
scope of this simple example.

## Summary

Hazelcast provides some standard distributed objects for maps, queues,
lists, sets with pre-defined behavior. These are configurable up to
a point.

You can add your own objects using the Service Provider Interface (_SPI_),
and these will run *inside* Hazelcast alongside all the pre-defined
structures. The methods made available for managing your objects are
the same ones as the internal objects use.