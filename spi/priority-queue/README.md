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

## Services, proxies &amp; operations

The _SPI_ works with _services_, _service proxies_ and _operations_.
These are used for the built-in Hazelcast distributed objects and
for custom objects.

* The first concept is of a _service_. 

The service runs on the grid somewhere, and has its lifecycle of
creation and deletion managed by Hazelcast.

The service manages multiple priority queues, each of which is
identified by a name.

What this means in practice is when priority queue with name "_xyz_"
is needed, the hosting workload is distributed onto the grid.

The queue instance with name "_xyz_" is hosted on *one* of the
available Hazelcast service instances in the grid, as queues
aren't striped across servers.

We don't control which Hazelcast server hosts this queue.
Hazelcast decides this, and further may need to move queue
from host to host if a data re-balance is run.

* The second concept is the _service proxy_.

As above, the _service_ for a queue exists on one Hazelcast server instance
in the grid.

A _service proxy_ is created on any Hazelcast server instance that
needs to work with the _service_.

A Hazelcast instance that needs to work with the _service_ is
provided with a _service proxy_ object instead. All the necessary
operations are available, but as the name suggest this is just
a local wrapper onto something that is actually elsewhere.

What this means is the local process doesn't need to know or
care if the object being accessed is local or remote, the
proxy handles it.

Even if the _service_ is on the same Hazelcast instance as the
code trying to use it, a _service proxy_ is provided. This
makes for uniform behaviour should the underlying data structure
(here a priority queue) be migrated to a different Hazelcast
server.

* Finally there is the _operation_.

An _operation_ is how the _service proxy_ works with the
_service_. It is a runnable object that can be sent from
place to place.

The _service proxy_ creates an _operation_, which may have
arguments and may return a value, a remote function
essentially.

Hazelcast looks after delivering the _operation_ from
the _service proxy_ to the _service_, running the
_operation_ at the destination, and delivering any
result back from the _service_ to the _service proxy_.

## The solution detail

The objective here is a distributed priority queue,
which we implement using operations, services and
service proxies.

### `MyPriorityQueue` interface

This defines how the queue will behave. Operations are defined to add items
to the queue (`offer(E)`), take items from the queue (`poll()`) and confirm how
many items are in it (`size()`).

The real thing would have more methods defined, but wouldn't add anything
to understanding.

The interface also specifies this is a `DistributedObject` so we also get
all the Hazelcase framework operations such as `getName()`. We can define
multiple such queues as named object on the grid and distinguish them by name.

### `MyPriorityQueueService` class

The _service_ class is where the real work happens. The _service proxy_ below
just provides a way to use it from anywhere in the grid.

There are three components to this

* `ManagedService`

Firstly, we define this to be a _ManagedService_.

Meaning, its lifecycle is managed by Hazelcast. Hazelcast will call `init()` and
`shutdown()` methods when the service is born and when Hazelcast is shutting down
to close the service. These allow us to do any extra activities such as opening
and closing resources that Hazelcast doesn't know about.

* `RemoteService`

Secondly, we define this to be a _RemoteService_.

Meaning, we can access this service remotely via a _service proxy_.
We provide the methods to create the queue proxies and tidy up when
queues are deleted.

* Finally, the priority queues

The solution needs priority queues!

Fortunately, Java provides them for us. So all the _service_ needs to
do is store a collection of `java.util.PriorityQueue`.

So what is here is a standard Java map, `Map<String, PriorityQueue>`
where each of the named priority queues are stored.

* PriorityBlockingQueue ?

As an aside, consideration needs to be given to thread safety.

Why is `PriorityQueue` adequate, when `PriorityBlockingQueue` would seem
necessary ?

The answer here is in Hazelcast's threading model.

Data storage in Hazelcast is split into parts called partitions,
and these are spread across the available servers in the grid.
Each server process hosts some of the partitions and has some
worker threads allocated to partition operations.

Crucially there is a _one:many_ mapping between worker threads
and partitions. Each worker thread looks after some partitions.
No partition is shared amongst worker threads.

If there are two threads and six partitions, this would mean
that `thread-1` looks after partitions _1_,_2_ &amp; _3_ and `thread-2`
looks after partitions _4_,_5_ &amp; _6_. If this becomes
a bottleneck, just increase the thread count.

Since the _service_ sits in a partition, and exactly one
thread is responsible for the partition, then only that
thread can be accessing the queue in the _service_.
Consequently we don't have to worry about concurrent access
from multiple threads to the same object.

So since `PriorityQueue` or `PriorityBlockingQueue` can be used, the former
is preferable as performance is higher.

(For the same reason, a standard map and not a concurrent map
is fine for storing the queues in the _service_).

### `MyPriorityQueueServiceProxy` class

The _service proxy_ has to provide implementations of the
methods in the queue interface ("_offer()_", "_poll()_", "_size()_").

It does this by creating _operations_ and sending them for execution
to the _service_.

For example, the implementation in the _service proxy_ for the "_size()_"
method looks like this:

```
public int size() throws Exception {
	MyPriorityQueueOpSize myPriorityQueueOpSize = new MyPriorityQueueOpSize();
	myPriorityQueueOpSize.setName(this.name);
		
    	InvocationBuilder builder = 
    		this.nodeEngine.getOperationService()
           .createInvocationBuilder("MyPriorityQueueService", myPriorityQueueOpSize, this.partitionId);

	Future<Integer> future = builder.invoke()
	return future.get();
}
```

* A `myPriorityQueueOpSize` operation is created, and the name of the queue
needing sized is provided.

* An operation invocation is created, to send the operation to the
service on one of the partitions in the grid.

The grid contains multiple data partitions shared across the servers.
If there were four partitions and two servers, it might work out
that `server-1` is hosting partitions _1_ &amp; _2_, and `server-2` is
hosting partitions _3_ &amp; _4_.

So what this means is the operation is to be sent to the server
currently hosting the specified partition.

The _NodeEngine_ is the accessor to Hazelcast internals, the
core of Hazelcast.

* We invoke the operation, and wait for the result of the execution.

### `MyPriorityQueueOpOffer`, `MyPriorityQueueOpPoll` &amp; `MyPriorityQueueOpSize` classes

These are the operations that `MyPriorityQueueServiceProxy` sends to
`MyPriorityQueueService` to do the actual work.

All are serializable and runnable.

The caller (`MyPriorityQueueServiceProxy`) creates the operation and adds any required
arguments. For example, for `MyPriorityQueueOpOffer` the item being offered to the
queue is a constructor argument.

When the operation is submitted by the _service proxy_, Hazelcast takes care
transporting the operation from the _service proxy_ to the _service_, as
these may be on different JVMs.

Once the operation arrives at the _service_, the `run()` method is invoked.

The `run()` method does the actual work, accessing the _service_ and doing
the necessary.

Since we're not implementing the priority queue from the ground up, the
implementation of these operations is easy. All we need do is retrieve
the right `java.util.PriorityQueue` object from the _service_, and use
it's methods to provide the operation response.

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

The sample solution uses [Spring Shell](https://projects.spring.io/spring-shell/)
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

#### Step 5 : Bonus

When we create the `IQueue` named "_vanilla_" in the `write()` call in the `CLICommands.java`
class the code is this:

```
com.hazelcast.core.IQueue<Order> vanilla = this.hazelcastInstance.getQueue("vanilla");
```

But if you have trace logging enabled, the `size()` call shows this

```
07:41:19.216 TRACE Spring Shell com.hazelcast.samples.spi.CliCommands - Distributed Object, name 'vanilla', class 'com.hazelcast.collection.impl.queue.QueueProxyImpl' 
```

We asked for a `IQueue` and instead we got a `QueueProxyImpl`. We get a _service proxy_
for the Hazelcast built-in objects, just like for our own custom object.

## What the sample solution is missing

To keep the sample simple, some parts have been omitted.

`MigrationAwareService` and `BackupAwareOperation` are not yet implemented.
This means there is only one copy of each priority queue, no backup,
and should the cluster change in capacity the queues aren't moved
from server to server to re-balance the data load.
This is covered by [this example](https://github.com/hazelcast/hazelcast-code-samples/tree/master/spi/backups) 
and would be a necessary step for a production strength implementation.

Client access is not yet implemented, but would be a more serious coding
effort. In Hazelcast, the client-server protocol is public -- see
[here](https://github.com/hazelcast/hazelcast-client-protocol/raw/v1.2.0/docs/published/protocol/1.2.0/HazelcastOpenBinaryClientProtocol-1.2.0.pdf).
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
