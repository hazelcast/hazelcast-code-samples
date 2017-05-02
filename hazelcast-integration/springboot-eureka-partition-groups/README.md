# Partition Groups with Eureka

[Diagram1]: src/site/markdown/images/diagram1.png "Image diagram1.png"
[Screenshot1]: src/site/markdown/images/screenshot1.png "Image screenshot1.png"
[Screenshot2]: src/site/markdown/images/screenshot2.png "Image screenshot2.png"
[Screenshot3]: src/site/markdown/images/screenshot3.png "Image screenshot3.png"
[Screenshot4]: src/site/markdown/images/screenshot4.png "Image screenshot4.png"
[Screenshot5]: src/site/markdown/images/screenshot5.png "Image screenshot5.png"
[Screenshot6]: src/site/markdown/images/screenshot6.png "Image screenshot6.png"
[Screenshot7]: src/site/markdown/images/screenshot7.png "Image screenshot7.png"
[Screenshot8]: src/site/markdown/images/screenshot8.png "Image screenshot8.png"
[Screenshot9]: src/site/markdown/images/screenshot9.png "Image screenshot9.png"
[Screenshot10]: src/site/markdown/images/screenshot10.png "Image screenshot10.png"
[Screenshot11]: src/site/markdown/images/screenshot11.png "Image screenshot11.png"
[Screenshot12]: src/site/markdown/images/screenshot12.png "Image screenshot12.png"
[Screenshot13]: src/site/markdown/images/screenshot13.png "Image screenshot13.png"
[Screenshot14]: src/site/markdown/images/screenshot14.png "Image screenshot14.png"
[Screenshot15]: src/site/markdown/images/screenshot15.png "Image screenshot15.png"
[Screenshot16]: src/site/markdown/images/screenshot16.png "Image screenshot16.png"
[Screenshot17]: src/site/markdown/images/screenshot17.png "Image screenshot17.png"
[Screenshot18]: src/site/markdown/images/screenshot18.png "Image screenshot18.png"
[Screenshot19]: src/site/markdown/images/screenshot19.png "Image screenshot19.png"
[Screenshot20]: src/site/markdown/images/screenshot20.png "Image screenshot20.png"

In this example we'll look at using Netflix's [Eureka](https://github.com/Netflix/eureka)
as a both a mechanism for process discovery in the cloud and as a way to specify data
safety rules for partition groups.

You could use Eureka for either, but it makes more sense here to do it for both.

In the real world, Eureka is for cloud deployments, meaning there are multiple machines
being used and these are usually spun up on demand so we don't know their details in
advance.

For this example, we will use a single host as this might be all you have access to.

## The Problem

Hazelcast IMDG is normally run as a *clustered* application, meaning several process
instances join together to form a group. This group shares the load of data hosting
and data processing.

Running IMDG as a singleton isn't wrong or unknown, it would just be a group of one.
You would still get all the benefits of memory speed and wouldn't have to be worried
about network transmission. But you wouldn't get the scaling or resilience to failure
from a single process.

So it is more typical to run multiple process instances in the group. If you have 10
instances and usage grows by 10%, you can just add an 11<sup>th</sup> instance. It can be as simple as that.

But, of course, there are complications. And for the purpose of this example, two to focus on.

### Problem 1 - Discovery

If you run more than one process and want them to join together, they need to know how to
find each other.

Fundamentally, there are two approaches, you try to contact everyone or you try a subset.

For brevity, we shall assume that contacting everyone - broadcasting via multicast - is
blocked.

For the solution where a selection of machines are possible, the simplest solution is
to cite them. IMDG instance __A__ tries to find IMDG instance __B__ on machine
_123.456.789.012_  because that machine address is specified in a config file somewhere.
It's a simple and easy solution, and in many cases all you need.

The problem comes on cloud and/or virtualised environments. If machines are
allocated when needed, you are unlikely to know their hostnames or IP addresses in
advance, so cannot name them in a config file.

*Problem 1* is that you can't tell one instance the location of another instance if you
don't know this yourself.

### Problem 2 - Data Safety

Data has varying value to a business, and the most valuable you don't want to lose. It
may be time-consuming or impossible to regenerate.

While the best answer is to stop failure in the first place, it would be sensible to assume
failure will still occur and to mitigate against this.

For Hazelcast IMDG, what this means is that you don't keep only one copy of valuable data
records in the cluster.

If you've configured for two copies, you only have data safety if a mishap isn't going
to impact them both. You want the data placement algorithm to choose two instances in the
cluster that won't fail together.

This is easy to ask for, but depends on some factors which the IMDG has no visibility of.
For example, which machines share a power supply.

*Problem 2* is to find a way for the IMDG to select which instances host which data
records when the best choice depends on factors that are hidden.

### The Problems Summarised

We have two problems to solve:

1. IMDG can't be pre-configured to specify hosts that are built at runtime.

2. Data mirror copies need to be placed on machines that won't fail together.

## The Solution

Eureka provides our solution!

### Solution to Problem 1 - IMDG can't be pre-configured to specify hosts that are built at runtime

Eureka is a writeable registry. 

It is realistic to pre-configure the location of Eureka, as this is not a dynamic
component. An environment, such as Production, might have a Eureka service but 
this will be a constant in the environment. It will not move about, and will be
started before everything else.

So, when an Hazelcast IMDG process starts it can know the location of the Eureka registry,
look in this registry to see which IMDG processes have already recorded their presence in
this registry, and record itself in the registry for the subsequent IMDG processes to
note.

#### DNS aliases

As an aside, DNS aliases is another way to solve problem 1.

IMDG instance __A__ could refer to the machine that hosts IMDG instance __B__ with
a name such as _imdg\_instance\_b_ host. When that machine is known, we just add an
entry to the DNS matching that floating name to the IP address actually being used
and flush the DNS.

It would work, though might not be popular to update the DNS frequently.

#### It is realistic to pre-configure the location of Eureka

Is it ?

Much of the nature of this problem hinges on what it is realistic to expect to
be fixed and what is not.

In a cloud environment it's not particularly contentious to say that it's difficult
to know all of the Hazelcast hosts in advance. Hazelcast IMDG is scalable, so the
number can go up and down. This certainly makes it impossible to know them all.
If the group size is unknown it's not surprising that make cloud systems will
not allocate host names until the last minute.

Eureka plays a different role in an environment, as it's more of a support service
and has no need to be scalable. So the number of hosts for Eureka usually won't
vary.Again though, this does not guarantee that any of these hosts will be
allocated until the Eureka service is started.

The difference is with timing.

The Eureka service must start before the Hazelcast service as Eureka provides
a service to Hazelcast. So even if the Eureka hosts aren't known before
Eureka starts, they are known after Eureka starts and this is before
Hazelcast starts.

### Solution to Problem 2 - Data mirror copies need to be placed on machines that won't fail together

Normally what Hazelcast IMDG has access to is the IP addresses for the JVMs.

As per this diagram, imagine there are four JVMs, one per host machine.

![Image of processes on machines that only appear to be independent][Diagram1] 

The host machines have IPs _10.20.30.10_, _10.20.30.20_, _10.20.30.30_ and _10.20.30.40_.
But of the four machines there are only two power supply units (PSUs).

So if a data record is placed on _10.20.30.10_, then placing the backup copy on
_10.20.30.20_ woud mean both copies would be lost if the PSU on the left failed.

If the data record goes on JVM _1_ on the leftmost, IP _10.20.30.10_, then
JVM _2_ is not a great place to put the backup of that data record. Though
we should note it is still better than no backup at all! 

Hazelcast IMDG normally only has access to the machine's IP addresses, and
there's not much to compare _10.20.30.20_ with _10.20.30.30_ when picking one.

The solution is to guide Hazelcast, by indicating which machines are related
by the underlying infrastructure, such as power supplies.

Of course, power supplies are just an example. But at some point something is
shared, whether it is cables, cabinets, power supplies or even buildings.

## The Solution In Action

So let's see how it's done.

We have named the solution *Eurekast*, as a portmanteau of _Eureka_ and _Hazelcast_.

In this example, the project structure is 5 modules:

```
├── my-eureka-common/
├── my-eureka-server/
├── my-eureka-client/
├── my-hazelcast-server/
├── my-hazelcast-client/
├── pom.xml
├── README.md
```

We shall describe each module individually before we do the follow-along example of their
usage.

Apart from the `my-eureka-common` module, the main four modules are all Spring Boot executable *jar* files,
using configuration specified in their `bootstrap.yml` files.

### `my-eureka-common`

This module contains common code used by all the others.

The important class here is `MyEurekaDiscovery` which does the hard work of finding things in Eureka.
There are only really two methods in here that do anything useful, which we shall describe below as
they get invoked.

### `my-eureka-server`

This module creates a single Eureka server, and stores some configuration in it for Hazelcast
to find.

Ordinarily you would run multiple Eureka servers clustered together rather than just one,
in the same way as multiple Hazelcast IMDG servers are clustered here. This would be a
useful first step in productionizing this example.

### `my-eureka-client`

This is an optional diagnostics module, you can ignore it if you like.

What this module does is connect to the Eureka server and log the _Eurekast_ data stored
in it.

This is useful for debugging. If the Hazelcast server can't find what it needs in the Eureka
server, how else will you know which one is wrong.

### `my-hazelcast-server`

As you might guess, this module is for a Hazelcast server.

In this example, we'll run several copies of this same module, to see how they obtain
what they need from the Eureka server, and form a cluster with a controlled level of
data safety.

### `my-hazelcast-client`

This module is a Hazelcast client that connects to the Hazelcast server(s).

It uses the Eureka server to find the location of the Hazelcast servers to connect
to, but also displays the data stored in the Hazelcast servers.

Seeing what data is stored in the Hazelcast servers will prove the data has
been safely stored when we come to kill some Hazelcast servers.

## Checkpoint 1 - Discovery Service

Hazelcast IMDG is a scalable resilient cluster of processes. You can start and stop processes
to adjust to capacity. Every process in the cluster is updated when a process joins or leaves,
so always knows the cluster member list.

The need for a discovery service is for that initial connection. All you need do is
find another process that is in the cluster, and it can inform you of the full list.

In general terms all the discovery service has to do is itemize some of the processes
that in the cluster, it doesn't matter if it's not a complete list as you'll get the
rest once you join.

The method signature for discovery is just this:

```
public Iterable<DiscoveryNode> discoverNodes()
```

All that is required is to return a list of `host`:`port` pairs to find one to use
for that initial connection.

Normally this is built from `hazelcast.xml` file using a static list.
All that's different here is we're doing it dynamically.

## Checkpoint 2 - Data Safety & Partition Groups

At this point, it's worth a quick recap on what is really meant by data safety
and partition groups.

### Data Safety

Data safety in Hazelcast IMDG is **configurable**.

Data is "_safe_" if you have more copies than you might lose.

If you have Hazelcast in the default configuration, "`<backup-count>1</backup-count>`", then
map data has two copies, there is a master and one backup slave. You can lose *one* copy
and still have another copy left. 

If you've configured Hazelcast for a higher configuration, such as "`<backup-count>2</backup-count>`", then
map data has three copies, a master and two backup slaves. Here you can lose *two* copies and
still have one copy left.

So what this boils down to is data is safe in the cluster if you don't lose all the copies.
If you do lose all the copies in Hazelcast, and have no other copies elsewhere (eg. in an RDBMS)
then it's gone for good. For most but not all data this would be a problem.

The naive answer is just to increase the number of copies, but this has cost in terms of storage
and performance.

Another way to look on it is if you're expecting more than one or two machines to fail, you've
bought some bad hardware. More of this hardware wouldn't be a good idea.

#### RAID

The astute reader will have spotted that this is exactly the same as disk mirroring.

Disks are not infallible. You mirror disk data onto multiple disks for the exact same
reason, to cope with some but not all failing.

### Partition Groups

When machines failing is mentioned above, the implication is for unrelated failure.

Machine __A__ fails but there's another copy of the data on Machine __B__ so we're good.

The problem might come if a single event takes out both Machine __A__ and Machine __B__,
for example if they are in the same cabinet and this catches fire.

So it's not always sufficient to have more than one copy of the data in the
Hazelcast cluster, the copies must be kept apart to protect from such issues
and that's where *Partition Groups* comes in.

Hazelcast IMDG runs in a JVM, so only has access to what the JVM can tell it.
It can know the IP addresses of which hosts support a cluster, but can't
know which of these are in the same cabinet, share a power supply, or
any such information. 

So you have to tell it, job done.

It's actually very easy. All you need do is come up with a list of
labels for the groups, and associate hosts with each. Then Hazelcast
has all the information it needs.

So, for example, imagine four hosts in two cabinets.

```
+-------------+---------+
|    Host     | Cabinet |
| 12.34.56.11 |    l    |
| 12.34.56.22 |    l    |
| 12.34.56.33 |    r    |
| 12.34.56.44 |    r    |
+-------------+---------+
```

The partition groups could be "`l`" for left and "`r`" for right. As simple as that.

If Hazelcast IMDG stores the master copy of a data record on host `12.34.56.11`
then that's in group `l`. So the backup copy of that data record doesn't go
to a group `l` host. The backup copy goes to `12.34.56.33` or `12.34.56.44`
but not to `12.34.56.22`

This means cabinet "l" can fail, removing two machines from the four in the
cluster but not losing both copies of that data record.

#### Caveats

Of course, there are other concerns to remember.

For a start, it's not just about data storage you have also to remember data
processing. If you lose a cabinet as in the above example, you lose 2 of the
4 JVMs in the cluster, so will the remaining 2 be able to support the
traffic ?

And you need to be very aware of infrastructure moves. Data safety here
comes from knowledge of the physical aspects of machine set-up, but
someone in the future might move a machine from cabinet to cabinet,
and not adequately communicate this.

## Build

The example uses Spring Boot for forming executable _jar_ files.

It's easiest to use `mvn install` from the top level to build everything, as this makes sure all the Spring Boot
repackaging phases happen. You can run it from an IDE as well, depending on the IDE, but we'll just use the
command line.

## Running The Solution

Reading the code is one thing, the proof comes from trying it.

So what we are looking to show here is:
1. Hazelcast instances use Eureka to obtain the location of other Hazelcast instances
2. Hazelcast instances use Eureka to determine which Hazelcast instances should host which data copies.
3. Killing some Hazelcast instances need not cause data loss, a cluster can be as resilient as you want.

### 1. Start Eureka Server

The first step is to start the Eureka server, which will start and stay running.

```
java -jar my-eureka-server/target/my-eureka-server-0.1-SNAPSHOT.jar
```

This will produce a lot of output from all the embedded services, but once started
you should see messages like the below:

![Image of Eureka server console output][Screenshot1] 

It's the _HTTP 204_ status code you're looking for. This means it's good but no output.

Once this Eureka server is started, leave it running for the duration. Remember here we only
run one Eureka server and for real you would run multiple clustered together.

At any point, you can go to http://localhost:8761 to see what Eureka has recorded.
Try this now.
You should see a line in the *Application* section for the *EUREKAST* application.
At the right of this line is a list of clickable URLs for the components of this
application, and this will go up and down as Hazelcast servers join and leave,
though you will need to refresh the URL.

![Image of Eureka server on a web browser][Screenshot2] 

#### The code : `my-eureka-server` => `MyEurekaServer.java`

There is very little to the code, as Spring Boot and Spring Cloud provides the required functionality.

All we do is use the `@EnableEurekaServer` annotation to make this process into a Eureka server, and set
a system property for the `bootstrap.yml` file.

#### The code : `my-eureka-server` => `bootstrap.yml`

This is the config file used by the Eureka server, and should be largely self-explanatory apart from this:

```
      hazelcastZone:
        localhost.8081: odd
        localhost.8082: even
        localhost.8083: odd
        localhost.8084: even
        localhost.8085: odd
        localhost.8086: even
        localhost.8087: odd
        localhost.8088: even
        localhost.8089: odd
        localhost.8090: even
```

These settings allocate groups to Hazelcast servers that may be started.

Meaning, if we start a Hazelcast server on `localhost` on port `8081` its group label is **odd**.
If we start another Hazelcast server on `localhost` on port `8083` it is also in group **odd**.

Using **odd** and **even** as group names is just arbitrary, we could have used **hi** and **low**.

### 2. Run Eureka Client

At this point, you should run the Eureka test client, which should start, connect to the Eureka
server, log what it finds, then shut down.

```
java -jar my-eureka-client/target/my-eureka-client-0.1-SNAPSHOT.jar
```

Again there will be a lot of output, but what you're looking for here is partition group
specification to be output, which proves it is stored in the Eureka server.

![Image of Eureka client console output][Screenshot3] 

#### The code : `my-eureka-client` => `MyEurekaClient.java`

There is some code here which is worth a glance at, as it's similar to how the Hazelcast
discovery service interrogates Eureka.

We use Spring to instantiate a `DiscoveryClient`, which is effect is a Eureka client that has
already connected to the Eureka server.

With this `DiscoveryClient`, what we retrieve is the **EUREKAST** application information,
and list what data is stored in the application. This data is just a list of _key-value_ pairs
both strings.

#### The code : `my-eureka-client` => `bootstrap.yml`

Again the `bootstrap.yml` file gives the configuration, here really just the address of the Eureka server.

### 3. Start a first Hazelcast Server

All the modules are standalone executable _jar_ files, so starting the first of the Hazelcast
servers is easy enough.

```
java -jar my-hazelcast-server/target/my-hazelcast-server-0.1-SNAPSHOT.jar
```

There are two things to look out for here, showing how the Hazelcast IMDG server
has connected to the Eurkea server and found the configuration data stored there.

The first output is for the partition groups. This is the 1<sup>st</sup> Hazelcast
IMDG server to start so we are expecting to see that it has been assigned the odd
numbered partition group zone.

![Image of Hazelcast server partition group][Screenshot4] 

The second output is for server discovery. As this is the 1<sup>st</sup> Hazelcast
IMDG server to start, no other servers should exist in the cluster group, so there
should be no servers already registered in Eureka.

![Image of Hazelcast server discovery][Screenshot5] 

It's time to look at the server's code to see how this is achieved.

#### The code : `my-hazelcast-server` => `MyConfiguration.java`

This sets up the configuration for the Hazelcast IMDG server, with three
tweaks.

1. Discovery

Normally the way for IMDG processes to find each other is specified with
the `network` section in the `hazelcast.xml` file.

Here it's all Java, and we have selected instead a discovery plug-in to
do the work.

Spring Boot will inject a `DiscoveryServiceProvider` bean, which is
set up in the `my-eureka-common` module.

2. Zones

The IMDG instance is configured to use discovery zones, meaning we will
supply information to mark servers are related (in the same zone) or
unrelated (in different zones).

Zones here is just the nomenclature, but it could correspond with the
zones of a cloud provider, such as _USA North-East_ and _USA North-West_.

3. Map configuration

We need to **prove** that the zones actually work.

So we provide the configuration for two maps.

The "__eurekast_safe__" map has backups, so it reasonably insulated from JVM failure.

The "__eurekast_unsafe__" map has no backups, so is not insulated from JVM failure.

#### The code : `my-eureka-common` => `MyEurekaDiscoveryService.java`

In the `my-eureka-common` module the `MyDiscoveryServiceProvider` always returns
the Spring `@Bean` for the `MyEurekaDiscoveryService` class.

This class does all the hard work in this example so is worth a very
close look, but it only has two methods that do anything.

1. `discoverLocalMetadata`

This method is called first at start-up, to query the Eureka service
for data that applies to the cluster as a whole.

For this example, the only cluster wide data stored is the data storage
zones.

As we've noted above, servers with odd numbered ports go in the _odd_ zone
and those with even numbered ports in the _even_ zone.

So in effect, we have defined **two** storage zones. If a data record is
placed in one storage zone, it's backup should not go the same zone. Three
or four zones might be a better idea than two, but two is enough to provide
another zone whether backups could go.

2. `discoverNodes`

This method is called second at start-up, to see what IMDG servers are
known to Eureka.

In a traditional setup, the `hazelcast.xml` file lists the IMDG servers
in the cluster. So the addresses of some servers are preset.

Here instead we do not preset any IMDG server addresses, and instead get
the known servers from what is currently recorded in Eureka.

#### The code : `my-hazelcast-server` => `MyHazelcastServer.java`

Normally, the `main()` class in a Spring Boot process does almost nothing.

In this case, we set up some system properties, purely as a convenience
to running multiple Hazelcast IMDG server processes on the same machine.

To reiterate, it's generally best not to do this. One machine per JVM is
better as being the only process running on the host their share of the
CPUs is confirmed. You might run multiple JVMs per host if budgets are
tight. Here we run multiple JVMs per host as we do not know how many
hosts are available so are aiming for one host for the entire cluster.

To allow us to run multiple IMDG servers on the same cluster we have
to avoid port clashes. Fixed ports, as we would normally prefer won't
work for multiple JVMs on the same hardware. Random ports, where we
let Spring pick any port it can find would do, but it's trickier for
testing. So what we do is systematically look for unused ports in a
sequence.

All of which is a rather wordy way to confirm that the port for web
traffic used is 8081 if this is available, then it tries 8082, then
it tries 8083.

#### The code : `my-hazelcast-server` => `bootstrap.yml`

The `bootstrap.yml` file controls the Eureka access, mainly that
when this process connects to Eureka it registers itself with
some meta-data.

The meta-data is the host and (Hazelcast) port for this instance.

These are set in `MyHazelcastServer.java`.

#### The code : `my-hazelcast-server` => `TestDataLoader.java`

A last piece of coding injects some test data into the cluster.

Here we do it from the Hazelcast server. If the maps "__eurekast_safe__"
and "__eurekast_unsafe__" are empty, we put some data in them.

The maps "__eurekast_safe__" and "__eurekast_unsafe__" are 
[IMap](http://docs.hazelcast.org/docs/3.8/javadoc/com/hazelcast/core/IMap.html),
meaning they are split into sections and those sections spread across
the available servers. The default is for 271 such sections, named _partitions_,
so we create 271 entries to try to put one data record entry into each.

#### Reminder : Eureka sequence is query then register

A final point to note on the Hazelcast server is that the Eureka
connectivity is provided by Spring with a discovery client bean,
activated in the main program with the `@EnableDiscoveryClient`
annotation.

This is used to read the Eureka server as this process starts, then
register this process with Eureka once it is fully started.

### 4. Run Eureka Client

Supposedly the Hazelcast IMDG server that has just started has registered
with Eureka.

Run the Eureka client to check.

In addition to the partition group specification seen in step 2, there
should now also be recorded the host and port of the Hazelcast IMDG
server just started.

![Image of Eureka client showing Hazelcast server host and port][Screenshot6] 

In this example the host is _192.168.1.156_ and the port _5701_. Unless
you've changed the code, you should have the same port.

### 5. Browse Eureka Server

Refresh your browser on the Eureka server's URL http://localhost:8761 

The *Application* section of the page will list the *EUREKAST* application but should now have two clickable
links for the instances of that application.

![Image of Eureka server browser window showing application instances][Screenshot7] 

Click the newest one added, and you should see the build information for the Hazelcast server just started.

![Image of the build timestamp for the Hazelcast server][Screenshot8] 

This build information is added by Spring Boot Actuator into the _jar_ file, and is a useful
way to confirm what is deployed.

### 6. Start a second Hazelcast Server

Repeat step 3, to start another Hazelcast IMDG server that will stay running.

The first thing to look for in the log output is the selected zone for partitioned
data. Here the web port is _8082_ so the zone is _even_. The first server started
was in zone _odd_.

![Image of second Hazelcast server zone][Screenshot9] 

The second thing to look for is what information is found in Eureka for existing
Hazelcast servers.

Here what we see is that the second Hazelcast server obtains the location of
the first Hazelcast server from Eureka.

This is the magic step! The second IMDG server has __discovered__ the first,
and now knows where to try to communicate with.

![Image of second Hazelcast server finding location of first][Screenshot10] 

Now that the second IMDG server knows the location of the first, it will
try to join with it. This should be successful, it's the same _jar_ file
for both with the same name/password coded.

So we should see a cluster listed with two members.

![Image of Hazelcast group listing two][Screenshot11] 

### 7. Browse Eureka Server again

Refresh your browser on the URL http://localhost:8761 

Now you should see three clickable links for the *EUREKAST* application in the *Application* section.

Two of these are for the Hazelcast servers started in steps 3 and 6. The other is for the partition group
specification set at start up in the Eureka server.

![Image of Eureka server browser showing three instances][Screenshot12] 

### 8. Start a third Hazelcast Server

Repeat step 6 to start a third Hazelcast server which will stay running.

As before, it will list the servers it finds in Eureka, and this time two will be already known.

At the top of this screenshot you'll see the port `8083` so the group is `odd`.
Towards the bottom of the same screenshot you'll see it has then obtained the
locations of the two previous Hazelcast servers, `192.168.1.156:5701` and `192.168.1.156:5702`.

![Image of third Hazelcast server showing group and other servers][Screenshot13] 

Once it completes startup, you will see the usual Hazelcast message about there being three
members in the cluster.

### 9. Start a fourth Hazelcast Server

Repeat step 8 to start one more Hazelcast server, which again should stay running.

Start-up messages should be as you expect, with the declared partition group for this process being
`"even"`, three previous server process locations found in Eureka, and finally
the usual cluster group information showing the group now has four.

![Image of fourth Hazelcast server showing group and other servers][Screenshot14] 

#### Checkpoint 3 - Cluster Members

At this point in the process there are 4 Hazelcast IMDG server processes running in a cluster.

Two have the `"odd"` partition group, the first and third started.

Two have the `"even"` partition group, the second and fourth started.

We're going to kill some of them off shortly, so you need to know the process ids of
each. Exactly how will depending on your operating system, but since they were
started in sequence probably the first IMDG has the lowest number the second IMDG
the next lowest number and so on.

### 10. Run Eureka Client

For one last time, run the Eureka client. This will start up, log what it finds in Eureka, then shut down.

In addition to the partition group from before, we should see that each of the 4 Hazelcast IMDG server
processes have recorded their presence in Eureka.

![Image of Eureka client showing registered Hazelcast servers][Screenshot15] 

At this point we don't intend to start any more Hazelcast servers, so the partition group specification
is no longer needed in Eureka. But we do want to start Hazelcast clients to connect to the Hazelcast
servers, and clients must find servers in the same way as servers find servers.

### 11. Run Hazelcast Client
Now the Hazelcast IMDG grid is up and running, we can run a Hazelcast client
to connect to it.

```
java -jar my-hazelcast-client/target/my-hazelcast-client-0.1-SNAPSHOT.jar
```

The client needs to find the Hazelcast servers, and it gets their
location from Eureka.

![Image of Hazelcast client discovering Hazelcast servers][Screenshot16] 

Now that the client has found and connected to the Hazelcast servers,
it can do some useful work.

For the purposes of this example, useful work means to count and display
the number of data record entries in the maps.

The counts for "__eurekast_safe__" and "__eurekast_unsafe__" maps should be 271, for the data
created by the first Hazelcast server that started in step 3.

![Image of Hazelcast client counting map content][Screenshot17] 

Although one of the maps is named "__eurekast_unsafe__" to reflect it's lower
data safety setup, it's not going to lose any data just because
of that configuration. Something has happen to make a cluster server go
offline quickly before there's a chance of data loss.

#### The code : `my-hazelcast-client` => `MyConfiguration.java`

The client configuration is much simpler, as the client does no
data storage it doesn't have to care about data safety and
partition group zones.

All really that is different from normal is discovery.

We provide the client's configuration with a discovery service
provisioning mechanism.

This is the `MyDiscoveryServiceProvider` class from the `my-eureka-common`
module, which always returns a Spring bean `MyEurekaDiscoveryService`.

In other words, the Hazelcast client finds the Hazelcast servers
using the same mechanism as the Hazelcast servers find the Hazelcast
servers.

#### The code : `my-hazelcast-client` => `MyHazelcastClient.java`

The entry point to the code is fairly easy, normal Spring
Boot coding to launch Spring plus a bit of Hazelcast map
access to display the map sizes.

Again, we activate `@EnableDiscoveryClient` so that Spring Cloud
will provide all the necessary connectivity to the Eureka
server for us.

#### The code : `my-hazelcast-client` => `bootstrap.yml`

The configuration here is simpler than the Hazelcast
servers.

We don't need to record with Eureka that Hazelcast clients
are running, so we turn off `registerWithEureka`.

And we don't care which HTTP port the client uses, so this
is set to 0 and one will be picked at random. Which port
is picked is listed in the start-up log messages.

### Checkpoint 4 - Disaster

What we're trying to simulate here are faults in the system,
such as hardware failure or a JVM crash. We don't really
care the cause, but we want it to be something instant, so
the cluster has no chance to take preventative steps.

On a Unix system a command such as `kill -9 1234` should
do the trick. On Windows, `taskkill /f /pid 1234`.

This is a really useful exercise to understand how your
cluster will behave when unplanned events occur.
You need to know when you should worry and when you
shouldn't.

What we're going to do is kill off processes in
one of the groups.

### 12. Kill a Hazelcast Server

Kill off the first Hazelcast server that was started,
the one running on web port 8081 and Hazelcast port 5701.

The survivors will notice it gone, and mention this in
their logs.

### 13. Run Hazelcast Client again

At this point the cluster has reduced to three processes.

Run the Hazelcast client to summarise the map content.

![Image of Hazelcast client counting map content again][Screenshot18] 

What you should see is that the "__eurekast_safe__" map has survived
this event unscathed. There are still 271 entries.

What you should see is that the "__eurekast_unsafe__" map has lost
some data. In the screenshot only 201 entries remain, so 70 have
gone.

You should expect to lost about a quarter of the unsafe (no backup)
data. Four servers, one is killed.

The exact number lost will depend on how the map partitions have been
allocated to the servers. This is not random, but difficult to predict.

### 14. Kill another specific Hazelcast Server

Kill off the third Hazelcast server that was started,
the one running on web port 8083 and Hazelcast port 5703.

What we're trying to simulate here is problems in the `_odd_` zone.

#### Checkpoint 5 - Bonus

There are two servers in the `_odd_` zone and we have killed them
off one at a time.

Why not repeat the steps, but kill off the first and third server
at the same time, see what happens.

### 15. Run Hazelcast Client yet again

Run the Hazelcast client again to see the map counts.

![Image of Hazelcast client counting map content for the third time][Screenshot19] 

The "__eurekast_safe__" map is still fine, all 271 entries are there.
We have lost both the `odd` zone servers but the other copy of the data was
in the `even` zone servers, so no data has been lost.

With two servers gone, the "__eurekast_unsafe__" map will be about half the
original size. Here it shows 149, you should have a roughly similar number.

### 16. Kill yet another Hazelcast Server

At this point, the cluster is half size. We've lost all (both) the `odd`
servers but haven't lost any of the map data configured with data safety
enabled.

Now kill one of the remaining two servers, one of the `even` servers.

It isn't really realistic to lost 75% of the servers and expect to keep
running, but we're doing it here to see what happens.

The reason it isn't realistic is that in a production system, there
would be a reason to run four servers, and it might be that four are
needed to service all the connected clients. Perhaps three or two
would cope with the workload intended for four, but it's a big
ask for one server.

### 17. Run Hazelcast Client one last time

Run the Hazelcast client yet again to count the maps.

![Image of Hazelcast client counting map content for the final time][Screenshot20] 

The "__eurekast_unsafe__" has lost even more data as we'd expect, but there are still all 271 entries in the "__eurekast_safe__".

Surprised ?

The partition group dictates that Hazelcast puts the backups in a different zone from the master.
That's fine and preserved our data across the loss of both `odd` servers.

Now there are only `even` servers the backups have moved to servers in the **same** zone. 
That's not as good as using the different zone we wanted, but if there isn't a different
zone available it's better than nothing. Here it has saved us.

Surprised 2 ?

The "__eurekast_unsafe__" has dropped from 149 to 145 by the most recent loss of a server.
We've lost 75% of the hosts but not 75% of the data, and that's because it's the partitions
that are spread across the hosts.

## Hazelcasts Mancenter

We've configured for data safety and use a client to show how data is lost or retained
depending on the configuration.

If you've the Hazelcast Mancenter, then this is much simpler to follow on the GUI.

## Changes For The Cloud

Mostly this example is good to go, but it has been adjusted slightly to run on a single machine.
There are two main adjustments.

### Ports

Best practice is to run a single JVM per host, which for Hazelcast servers gives the highest
degree of isolation from failures and gives a good assurance that access to machine resources
such as CPUs won't fluctuate due to the activity of other processes.

When you do this, as the only process on the host, you can preset the port.

So, for real you would want every Hazelcast server to use port 8080 for web traffic and 5701
for Hazelcast traffic for instance.

In `MyHazelcastServer`, it has to accommodate all servers running on the same host, and
so sets the web port in a series from _8081_ (ie. _8081_, _8082_, _8083_...) and the Hazelcast port
in a series from _5701_ (ie. _5701_, _5702_, _5703_...). This coding can be removed.

### Partition Group

The partition group configuration in the Eureka server is purely for demonstration purposes,
picking whether the port is odd or even to determine which group a server belongs to.

This should be replaced with whatever grouping information you have.

For example, this might be a group of servers on one floor of a building as one group,
and the servers on another floor as another group.

This needs replaced in the `bootstrap.yml` file of `my-eureka-server` with whatever values
are appropriate to your environment.

## Other Improvements

We've mentioned more than once that the Eureka server should be clustered.

Another improvement would be to inject the partition groups into Eureka, from a Eureka client,
rather than pre-load them.

## Summary

Pragmatic issues in your execution environment may make it unrealistic to pre-configure
all the environment settings you might need.

This isn't a blocker. All you need do is run some sort of configuration service
somewhere, and Hazelcast can pick up this configuration at runtime from the
configuration service.

In this example, the configuration service is Eureka as it's Java based, but others exist
such as Consol and Zookeeper.

Ultimately, something somewhere must be preordained. If a configuration service is
used, the location of this must be preset or injected by the execution environment.
The configuration service needs to be clustered, as if it's not available everything
else that is dependent upon is in trouble.

Very little code is needed, especially if you have Spring Boot and Spring Cloud
to help.
