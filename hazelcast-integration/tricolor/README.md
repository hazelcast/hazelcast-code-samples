# tricolor

[Screenshot1]: src/site/markdown/images/screenshot1.png "Image screenshot1.png"

Multiple classloaders, multiple Hazelcasts, multiple loggers and multiple colours -- all in the one JVM.

## Why ?

The normal deployment is for a JVM to contain a single Hazelcast instance, a client or a server.
This means that the instance can utilise all the resources available to that JVM.

In automated tests, it can frequently be useful to run multiple Hazelcast instances in the one
JVM so that it is easy to control the sequence that the instances interact with each other in
a predictable way.

Alternatively, it could be necessary to connect to two different clusters at once, to efficiently
copy data from one to another.

## The Classloader Constraint

If you run two Hazelcast instances in a JVM, by default they will both be instances of the same
class, `com.hazelcast.core.HazelcastInstance`.

What that means in JVM terms is that there are two `HazelcastInstance` objects
but their `getClass()` method refers to the same class object.

The purpose of this example is to show how to bypass this, to create several `HazelcastInstance`
objects, each with a unique class object.

For the purpose of this example, the goal is to have each using a different coloured logger.
Each logger uses a different colour, so that when multiple instances are logging in the one
JVM, it is *easy* to tell the logging messages produced by each instance.

The logger used is [Log4j version 2](https://logging.apache.org/log4j/2.x/), the successor to the original
Log4j which is now retired.

## The Example

The example is structured as follows

```
├── src/main/java/
├── ├── com.hazelcast.samples.tricolor/
├── ├── ├── Main.java
├── ├── ├── MyCallable.java
├── ├── ├── MyClient.java
├── ├── ├── MyServer.java
├── src/main/resources/
├── ├── blue.xml
├── ├── green.xml
├── ├── hazelcast-client.xml
├── ├── hazelcast.xml
├── ├── red.xml
├── pom.xml
├── README.md
```

### `Main.java`

Simply, this process creates three threads -- two to invoke Hazelcast servers, and one to invoke
a Hazelcast client. It starts them running, lets them finish, then shuts down. 

Internally, these three created threads use a `CountDownLatch` and a `ClientListener` to control
their sequencing. That isn't visible to `Main`, it just starts them off and waits.

What this class does differently from normal is in the `myLoadClass` method. This makes sure
that the created threads have different classloaders, and this allows them to have different
loggers, which in turn is what allows output to be logged in different colours.

#### `myLoadClass`

This method loads a class with a new, separate classloader. As we use this to create a thread,
the items this thread then loads -- Hazelcast and the logging framework -- are in that
separate classloader.

The mechanism is to sibling the current classloader. A reference is made to the resource
locations (URLs) that the current classloader searches, then we create a *new* classloader
from the parent of the current classloader with those resource locations.

In efect, we duplicate the current classloader. The newly created classloader has the
same ancestry and looks in the same places for resources (such as Hazelcast), but
when it finds them it creates a separate class instance.

### `MyCallable.java`
The callable object here is fairly straight-forward, but it implements three interfaces.

It implements `Callable`, so that it can be invoked by a thread executor.

It implements `Serializable`, so that it can be sent from one Hazelcast instance (the client) to
others (the servers).

And it implements `HazelcastInstanceAware` so that the Hazelcast instance it runs on is injected.

When it runs, it logs out which server it is running on. Because we will run it on all servers,
you should see this logging message appear in blue and in green.

When it completes, it returns the id of the server instance it ran on, and the client logs this.
So you will see this in red.

### `MyClient.java`

The client thread is started with the argument `red.xml`, so it will pick up its logging configuration
from a file of the same name, and produce log output in <span style="color:red">red</span>.

The thread waits on countdown latch until all the servers are adequately started, then
creates a Hazelcast client instance based on `hazelcast-client.xml`.

Use of a countdown latch is a safer mechanism to control execution of multiple Hazelcast
instances in the one thread. They could all be started at once, and the client would try
to connect to the servers a few times before giving up -- so this might work if the servers
start quickly, and might fail if your machine is slow.

This client then submits a callable, logs the output, and shuts down.

All that really deviates from typical usage is the class loader, a different one from the
class loader used by the two server threads.

### `MyServer.java`

Two instances are created for the `MyServer` class. One is passed in the argument `green.xml`,
uses that log file, and produces log output in <span style="color:green">green</span>. The
other is given the argument `blue.xml` for <span style="color:blue">blue</span> log output.

Each of these threads decrements a countdown latch when the Hazelcast server instance inside
it is fully started. This is used so the client instance in another thread does not try to
start until all servers are started. This gives a degree of predictability to the execution,
although the two servers are started in parallel so there is still some randomness.

Once started, the servers use a `ClientListener` to detect when the client has disconnected.
On the assumption the client disconnection means the client has finished, the server shuts
down.

## Run it
The code uses Spring Boot for packaging, so you need to run the build at least as far as the `package` phase of Maven,
with a command such as:

```
mvn install
```

Then run the executable _jar_ file produced:

```
java -jar target/tricolor.jar
```

Assuming you have multi-coloured output for your terminal window, you should see something like this:

![Sample output][Screenshot1] 

You should get a message from each of the server threads and client thread that each has started.
Probably you will get a message that the client thread is waiting for the server threads to be ready,
but you may not get this depending on your machine speed.

The client will submit a collable which will log on each server it runs on, then finally all three
threads will end and the process as a whole will close.
