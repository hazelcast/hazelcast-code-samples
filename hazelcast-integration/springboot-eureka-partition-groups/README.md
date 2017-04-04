# Partition Groups with Eureka

[Problem1]: src/site/markdown/images/problem1.png "Image problem1.png"
[Problem2]: src/site/markdown/images/problem2.png "Image problem2.png"

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

The problem comes on cloud and/or virtualised environments. If virtual machines are
allocated when needed, you are unlikely to know their hostnames or IP addresses in
advance, so cannot name them in a config file.

Problem 1 is that you can't tell one instance the location of another instance if you
don't know this yourself.

![Image of two processes unable to find each other][Problem1] 

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

So problem 2 is to find a way for the IMDG to select which instances host which data
records when the best choice depends on factors that are hidden.

![Image of three processes unable to decide where to place data][Problem2] 

### The Problems Summarised
We have two problems to solve:

1. IMDG can't be pre-configured to specify hosts that are built at run-time.

2. Data mirror copies need to be placed on machines that won't fail together.

## The Solution
Eureka provides our solution!

### Solution to Problem 1 - IMDG can't be pre-configured to specify hosts that are built at run-time

- [ ] Add text

#### DNS aliases
As an aside, DNS aliases is another way to solve problem 1.

IMDG instance __A__ could refer to the machine that hosts IMDG instance __B__ with
a name such as _imdg\_instance\_b_ host. When that machine is known, we just add an
entry to the DNS matching that floating name to the IP address actually being used
and flush the DNS.

It would work, though might not be popular to update the DNS frequently.

### Solution to Problem 2 - Data mirror copies need to be placed on machines that won't fail together

- [ ] Add text

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

Apart from the `common` module, the main four are all Spring Boot executable *jar* files,
using configuration specified in their `bootstrap.yml` files.

### `common`
This module contains common code used by all the others.

The important class here is `MyEurekaDiscovery` which does the hard work of finding things in Eureka.
There are only really two methods in here that do anything useful, which we shall describe below as
they get invoked.

### `my-eureka-server`

- [ ] Add text

### `my-eureka-client`

- [ ] Add text

### `my-hazelcast-client`

- [ ] Add text

### `my-hazelcast-server`

- [ ] Add text

## Running The Solution

- [ ] Add text

### Start Eureka

- [ ] Add text

## Changes For The Cloud

- [ ] Add text

## Other Improvements

- [ ] Add text

## Summary

- [ ] Add text
