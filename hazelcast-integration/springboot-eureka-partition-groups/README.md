# Partition Groups with Eureka

An example demonstrating the use of Netflix's [Eureka](https://github.com/Netflix/eureka)
for process discovery, and to dictate data backup placement. 

## The Problem
Most usually, Hazelcast IMDG is *clustered*, meaning several instances are joined together to
form a group. This group shares the load of data hosting and data processing.

Running a singleton isn't wrong or unknown - a cluster of one. You get all the benefits of
memory speed, querying, multi-threaded execution and don't have to worry about network transmission.
But you can't get the benefits of scaling or resilience to failure with just a singleton.

So, more usually you run multiple instances in a team. If you run 10 instances and your usage grows by 10%, you can just add an 11^th^ instance. It can be as simple as that.

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

- [ ] Diagram for dynamic machine allocation

### Problem 2 - Data Safety
Data has varying value to a business, and the most valuable you don't want to lose. It
may be time-consuming or impossible to regenerate.

While the best answer is to stop failure in the first place, it would be sensible to assume it
still will occur and to mitigate against this.

For Hazelcast IMDG, what this means is that you don't keep only one copy of valuable data
records in the cluster.

If you've configured for two copies, you only have data safety if a mishap isn't going
to impact them both. What this means here is you want the data placement algorithm to
choose two instances in the cluster that won't fail together.

This is easy to ask for, but depends on some factors which the IMDG has no visibility of.
For example, which machines share a power supply.

So problem 2 is to find a way for the IMDG to select which instances host which data
records when the best choice depends on factors that are hidden.

- [ ] Diagram for data placement

### The Problems Summarised
We have two problems to solve:

1. IMDG instances need to be told which machines host other instances, but we don't know the
machine names in advance.

2. Data mirror copies need to be placed on machines that won't fail together.


## The Solution



