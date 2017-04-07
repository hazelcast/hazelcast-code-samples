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
├── common/
├── my-eureka-server/
├── my-eureka-client/
├── my-hazelcast-server/
├── my-hazelcast-client/
├── pom.xml
├── README.md
```

We shall describe each module individually before we do the follow-along example of their
usage.

Apart from the `common` module, the main four modules are all Spring Boot executable *jar* files,
using configuration specified in their `bootstrap.yml` files.

### `common`

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

Here it's all Java, and we have selected instead a discovery plugin to
do the work.

Spring Boot will inject a `DiscoveryServiceProvider` bean, which is
set up in the `common` module.

2. Zones

The IMDG instance is configured to use discovery zones, meaning we will
supply information to mark servers are related (in the same zone) or
unrelated (in different zones).

Zones here is just the nomenclature, but it could correspond with the
zones of a cloud provider, such as _USA North-East_ and _USA North-West_.

3. Map configuration

We need to **prove** that the zones actually work.

So we provide the configuration for two maps.

The __safe__ map has backups, so it reasonably insulated from JVM failure.

The __unsafe__ map has no backups, so is not insulated from JVM failure.

#### The code : `common` => `MyEurekaDiscoveryService.java`

In the `common` module the `MyDiscoveryServiceProvider` always returns
the Spring <pre>@Bean</pre> for the `MyEurekaDiscoveryService` class.

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

TODO
TODO
TODO
TODO
TODO
TODO

#### The code : `my-hazelcast-server` => `bootstrap.yml`

#### The code : `my-hazelcast-server` => `TestDataLoader.java`

### 4. Run Eureka Client

![Image of what][Screenshot6] 

![Image of what][Screenshot7] 

![Image of what][Screenshot8] 

![Image of what][Screenshot9] 

![Image of what][Screenshot10] 

![Image of what][Screenshot11] 

![Image of what][Screenshot12] 

![Image of what][Screenshot13] 

![Image of what][Screenshot14] 

![Image of what][Screenshot15] 

![Image of what][Screenshot16] 

![Image of what][Screenshot17] 

![Image of what][Screenshot18] 

![Image of what][Screenshot19] 

![Image of what][Screenshot20] 

### 5. Browse Eureka Server

Refresh your browser on the Eureka server's URL http://localhost:8761 

The *Application* section of the page will list the *EUREKAST* application but should now have two clickable
links for the instances of that application.

Click the newest one added, and you should see the build information for the Hazelcast server just started.

- [ ] Add text - screenshot 3

### 6. Start a second Hazelcast Server

- [ ] Add text - screenshot 3

### 7. Browse Eureka Server again

Refresh your browser on the URL http://localhost:8761 

Now you should see three clickable links for the *EUREKAST* application in the *Application* section.

Two of these are for the Hazelcast servers started in steps 4 and 6. The other is for the partition group
information logged prior at start up in the Eureka server.

### 8. Start a third Hazelcast Server

Repeat step 6 to start a third Hazelcast server which wil stay running.

As before, it will list the servers it finds in Eureka, and this time two will be already known.

As it has an odd numbered port, it should declare it is using the `"odd"` partition group.

Once it completes startup, you will see the usual Hazelcast message about there being three
members in the cluster.

- [ ] Add text - screenshot 3

### 9. Start a fourth Hazelcast Server

Repeat step 8 to start one more Hazelcast server, which again should stay running.

Start-up messages should be as you expect, with the declared partition group for this process being
`"even"`.

#### Checkpoint 3

At this point in the process there are 4 Hazelcast IMDG server processes running in a cluster.

Two have the `"odd"` partition group.

Two have the `"even"` partition group.

- [ ] Add text - screenshot 3

### 10. Run Eureka Client

For one last time, run the Eureka client. This will start up, log what it finds in Eureka, then shut down.

In addition to the partition group from before, we should see that each of the 4 Hazelcast IMDG server
processes have recorded their presence in Eureka.

At this point we don't intend to start any more Hazelcast servers, so the partition group specification
is no longer needed in Eureka. But we do want to start Hazelcast clients to connect to the Hazelcast
servers, and clients must find servers in the same way as servers find servers.

- [ ] Add text - screenshot 3

### 11. Run Hazelcast Client

```
java -jar my-hazelcast-client/target/my-hazelcast-client-0.1-SNAPSHOT.jar
```

- [ ] Add text - screenshot 3

#### The code : `my-hazelcast-client` => `MyConfiguration.java`

#### The code : `my-hazelcast-client` => `MyHazelcastClient.java`

#### The code : `my-hazelcast-client` => `bootstrap.yml`

### 12. Kill a Hazelcast Server

- [ ] Add text - screenshot 3

### 13. Run Hazelcast Client again

- [ ] Add text - screenshot 3

### 14. Kill another specific Hazelcast Server

- [ ] Add text - screenshot 3

### 15. Run Hazelcast Client yet again

- [ ] Add text - screenshot 3

### 16. Kill yet another Hazelcast Server

- [ ] Add text - screenshot 3

### 17. Run Hazelcast Client one last time

- [ ] Add text - screenshot 3

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
