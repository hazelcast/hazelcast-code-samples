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

### The order queue

The data model here is orders from some sort of online shop.

See `com.hazelcast.samples.spi.Order` and `com.hazelcast.samples.spi.Day` in the common module.

In a real online shop, order objects will have a multitude of fields.
The customer's address for delivery, the item being ordered, the price,
and so on.

However, in this example, there are only really two fields to focus on
and both are sequential. 

The first is the order sequence number. Order 1 is created, then order 2,
and this progresses in the not exactly surprised sequence.

The second is the order due date. Orders are due for delivery at different
points. Order 1 might be due for delivery on Friday, and the following
order, order 2, might be due for delivery on Thursday.

So this is the challenge, orders will be raised in one sequence but must
be dispatched in another.

A simple queue isn't ideal. We can only put orders onto the queue as
they arise, but we need to remove the most pressing from the queue
first.

#### The data model detail

For simplicity in the example, the due date for orders is represented
by an `Enum` for days of the week. 

Days of the week are a fairly intuitive concept with a well-known and
natural sequence. 

An order due for Wednesday is more impending than an order due for Friday.

Of course, this is not exactly true. Because of the cyclical nature here,
if today is Thursday then the order due for Friday is more urgent than
the one due for Wednesday.

What is relevant here is we have a nature of compability. There is a
way to determine which order is the more important, even if the
algorithm is flawed.

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

### Sample solution steps

The sample solution uses (Spring Shell)[https://projects.spring.io/spring-shell/]
to provide a command line interface to interact with Hazelcast.

The commands are defined in the `com.hazelcast.samples.spi.CliCommands` class.

#### Step 1 : Write to the queues

From one of the servers, run the "_write_" command
(see the `com.hazelcast.samples.spi.CliCommands` class, the `write()` method).

This makes 5 orders, with sequence numbers _0_, _1_, _2_, _3_, and _4_ and writes them
in that sequence to two queues.

The queue named "_vanilla_" is a standard Hazelcast queue, an `IQueue`.

The queue named "_strawberry_" is our custom object, a `MyPriorityQueue`.

The same data is written to both, in the same order. Output should be like the below:

```
spring-shell>write
23:31:39.368 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 0 => Order(id=a7c2fc63-96ed-449c-a76d-066d9567a377, seqNo=0, dueDate=MONDAY) 
23:31:39.372 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 1 => Order(id=335060d8-065e-45f1-8005-44e6e9731f5f, seqNo=1, dueDate=THURSDAY) 
23:31:39.373 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 2 => Order(id=71453a2b-865e-489d-9e1c-eb75b86e955b, seqNo=2, dueDate=FRIDAY) 
23:31:39.383 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 3 => Order(id=9bb360ca-ee71-435b-9cc7-7658f55747d4, seqNo=3, dueDate=THURSDAY) 
23:31:39.384 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 4 => Order(id=fe109b71-8648-4104-8f52-05275c1d8543, seqNo=4, dueDate=TUESDAY) 
23:31:39.387 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Wrote 5 into queue 'vanilla', queue size now 5 
23:31:39.387 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 0 => Order(id=a7c2fc63-96ed-449c-a76d-066d9567a377, seqNo=0, dueDate=MONDAY) 
23:31:39.389 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 1 => Order(id=335060d8-065e-45f1-8005-44e6e9731f5f, seqNo=1, dueDate=THURSDAY) 
23:31:39.389 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 2 => Order(id=71453a2b-865e-489d-9e1c-eb75b86e955b, seqNo=2, dueDate=FRIDAY) 
23:31:39.390 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 3 => Order(id=9bb360ca-ee71-435b-9cc7-7658f55747d4, seqNo=3, dueDate=THURSDAY) 
23:31:39.391 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 4 => Order(id=fe109b71-8648-4104-8f52-05275c1d8543, seqNo=4, dueDate=TUESDAY) 
23:31:39.392 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Wrote 5 into queue 'strawberry', queue size now 5 
spring-shell>
```

#### Step 2 : Query the queue size

From one of the servers, run the command "_size_" to query the queue contents.
Again, if you are running more than one server, try this command from a different
one to the previous command, to show how both in-built and user-defined objects
can be accessed from everywhere.

This should show 5 items in the "_vanilla_" `IQeueu` and the same 5 items in
the "_strawberry_" `MyPriorityQueue`.
(see the `com.hazelcast.samples.spi.CliCommands` class, the `size()` method).

Something like this:

```
spring-shell>list
23:31:43.476 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
23:31:43.477 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Distributed Object, name 'strawberry', service 'MyPriorityQueueService' 
23:31:43.477 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands -  -> queue size 5 
23:31:43.478 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Distributed Object, name 'vanilla', service 'hz:impl:queueService' 
23:31:43.478 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands -  -> queue size 5 
23:31:43.478 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
23:31:43.478 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - [2 distributed objects] 
23:31:43.479 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
spring-shell>
```

#### Step 3 : Read from the queues

From one of the servers, run the command "_read_" to remove items from the queues.
(see the `com.hazelcast.samples.spi.CliCommands` class, the `read()` method).

```
spring-shell>read
23:31:47.030 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Queue 'vanilla' has size 5 
23:31:47.035 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 0 => Order(id=a7c2fc63-96ed-449c-a76d-066d9567a377, seqNo=0, dueDate=MONDAY) 
23:31:47.037 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 1 => Order(id=335060d8-065e-45f1-8005-44e6e9731f5f, seqNo=1, dueDate=THURSDAY) 
23:31:47.039 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 2 => Order(id=71453a2b-865e-489d-9e1c-eb75b86e955b, seqNo=2, dueDate=FRIDAY) 
23:31:47.040 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 3 => Order(id=9bb360ca-ee71-435b-9cc7-7658f55747d4, seqNo=3, dueDate=THURSDAY) 
23:31:47.042 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 4 => Order(id=fe109b71-8648-4104-8f52-05275c1d8543, seqNo=4, dueDate=TUESDAY) 
23:31:47.043 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Queue 'strawberry' has size 5 
23:31:47.045 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 0 => Order(id=a7c2fc63-96ed-449c-a76d-066d9567a377, seqNo=0, dueDate=MONDAY) 
23:31:47.046 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 1 => Order(id=fe109b71-8648-4104-8f52-05275c1d8543, seqNo=4, dueDate=TUESDAY) 
23:31:47.049 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 2 => Order(id=9bb360ca-ee71-435b-9cc7-7658f55747d4, seqNo=3, dueDate=THURSDAY) 
23:31:47.051 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 3 => Order(id=335060d8-065e-45f1-8005-44e6e9731f5f, seqNo=1, dueDate=THURSDAY) 
23:31:47.052 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Item 4 => Order(id=71453a2b-865e-489d-9e1c-eb75b86e955b, seqNo=2, dueDate=FRIDAY) 
spring-shell>
```

The important thing to note here is the order that items are retrieved.
If you check the code, for both queues the "`poll()`" method is used
to read items until the queue is empty.

The queue "_vanilla_" is a standard Hazelcast `IQueue`. Items are read in the order written.
So we should get back sequence numbers _0_, _1_, _2_, _3_, and finally _4_. 

The queue "_strawberry_" is our `MyPriorityQueue`. 
As our orders are `Comparable` we read the one with the highest priority. Priority is defined
by the due date of the week only. For days of the week that match (eg. both *THURSDAY*) we
take the first.

So, as you'll see, orders are retrieved the "_strawberry_" queue in a different order to the
way they are retrieved from the "_vanilla_" queue.

#### Step 4 : Query the queue size

From one of the servers, run the command "_size_" to query the queue contents.
Again, if you have more than one server, try this from a different one to the
previous command.

Having read from the queues, which removes the items, they should both now
be empty.

```
spring-shell>list
23:31:50.397 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
23:31:50.397 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Distributed Object, name 'strawberry', service 'MyPriorityQueueService' 
23:31:50.398 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands -  -> queue size 0 
23:31:50.399 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - Distributed Object, name 'vanilla', service 'hz:impl:queueService' 
23:31:50.400 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands -  -> queue size 0 
23:31:50.400 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
23:31:50.400 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - [2 distributed objects] 
23:31:50.400 INFO  Spring Shell com.hazelcast.samples.spi.CliCommands - ----------------------- 
spring-shell>
```

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