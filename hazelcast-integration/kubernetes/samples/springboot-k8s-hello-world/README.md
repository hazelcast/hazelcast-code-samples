# Hazelcast On Kubernetes Made Fairly Easy 

[Screenshot01]: src/site/markdown/images/screenshot01.png "Image screenshot01.png"
[Screenshot02]: src/site/markdown/images/screenshot02.png "Image screenshot02.png"
[Screenshot03]: src/site/markdown/images/screenshot03.png "Image screenshot03.png"
[Screenshot04]: src/site/markdown/images/screenshot04.png "Image screenshot04.png"
[Screenshot05]: src/site/markdown/images/screenshot05.png "Image screenshot05.png"
[Screenshot06]: src/site/markdown/images/screenshot06.png "Image screenshot06.png"
[Screenshot07]: src/site/markdown/images/screenshot07.png "Image screenshot07.png"
[Screenshot08]: src/site/markdown/images/screenshot08.png "Image screenshot08.png"
[Screenshot09]: src/site/markdown/images/screenshot09.png "Image screenshot09.png"
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
[Screenshot21]: src/site/markdown/images/screenshot21.png "Image screenshot21.png"
[Screenshot22]: src/site/markdown/images/screenshot22.png "Image screenshot22.png"
[Screenshot23]: src/site/markdown/images/screenshot23.png "Image screenshot23.png"
[Screenshot24]: src/site/markdown/images/screenshot24.png "Image screenshot24.png"
[Screenshot25]: src/site/markdown/images/screenshot25.png "Image screenshot25.png"
[Screenshot26]: src/site/markdown/images/screenshot26.png "Image screenshot26.png"
[Screenshot27]: src/site/markdown/images/screenshot27.png "Image screenshot27.png"
[Screenshot28]: src/site/markdown/images/screenshot28.png "Image screenshot28.png"
[Screenshot29]: src/site/markdown/images/screenshot29.png "Image screenshot29.png"
[Screenshot30]: src/site/markdown/images/screenshot30.png "Image screenshot30.png"

An step-by-step example to running Hazelcast on Kubernetes, the classic "_Hello World_"
style beginners introduction. 

The sample includes instructions and screenshots for running on Mac and Windows.

See [here](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/kubernetes/samples/springboot-k8s-hello-world) for the code.

You don't need to be an expert on Hazelcast or Kubernetes to make this work, as we'll
explain the terminology and details as we go. It's not trivial, but we'll make it
as simple as we can.

The code sample runs everything, including Kubernetes, on your computer. You don't
need access to a cloud provider of infrastructure. However, because it all runs on
your computer, your computer will need to have a reasonable degree of power for it
to work, as detailed in the next section.

## Pre-requisites

Kubernetes oversees a number of independent machine images, a bit like virtual
machines. In this sample these will all run on your computer.
So your computer needs enough strength to support all this load.

On a Mac, this sample has been validated on a 4 core machine with 16GB of RAM.

XXX Windows XXX

If you've less than this, it might just work or everything might grind to a halt as your
machine starts swapping. 

## Hazelcast concepts

Hopefully you're familiar with this, but a quick recap does no harm.

A Hazelcast server is a collection of Java objects that run within a JVM and
take responsibility for storing some data in memory. Other things can be
happening in that JVM, and if that's your business logic this is usually
described as "embedded" mode.

A Hazelcast client is another collection of Java objects that run within
a JVM, but don't take responsibility for storing data. Instead they
connect to Hazelcast server(s) across the network. Again, other things
can and usually will be happening in the Hazelcast client's JVM, and
this is described as "client-server" mode.

This example uses both Hazelcast clients and Hazelcast servers, each
of which are wrapped by Spring Boot into standalone deployment units.

So in fact here we have a mix of "embedded" mode and "client-server"
mode. Some business logic is in the Hazelcast server JVMs, and
some business logic is in the Hazelcast client JVMs, we put the
logic where it makes the best sense.

Hazelcast clients don't have to be Java of course, but that's what
they are in this example.

## Example without Kubernetes

The first step we'll take is to run the example without Kubernetes, Docker,
or all the other platform stuff.

Use `mvn clean install` to build the example from the top level.

### Hazelcast Management Center : `start.sh`

If you haven't already, you should download the latest Hazelcast Management Center
from [here](https://hazelcast.org/download/#management-center) and start
it up.

The start-up script is `start.sh`, this will launch an instance on port 8080 and
direct you to the login screen.

By default the Hazelcast Management Center uses your home directory to
hold set up information. If this the first time you've started it, you'll
be prompted to create an account for the "_admin_" user and set a password.

You'll come across this set up again once we add Kubernetes, but more on this later.

For now, you should log in to the Hazelcast Management Center, but we
haven't started a cluster so there won't be anything interesting to look at.

### Hazelcast Server : `java -Dserver.port=8081 -jar the-server/target/the-server.jar`

Start the Hazelcast server using the standalone _Jar_ file build by Maven.

Note we use `-Dserver.port=8081` so that the Tomcat web server component inside this _Jar_ file
binds to port 8081 to avoid clashing with the Hazelcast Management Center.

Towards the end of the start-up, you should see a message that 5 data records
have been loaded, like this:

![Image of Hazelcast server start-up][Screenshot01] 

#### Test the Hazelcast server

Try this command,

```
curl http://localhost:8081/k8s
```

This command exercises the REST controller added to the Hazelcast server's
JVM, that checks if the cluster is active.

So you should get output like this.

![Image of Hazelcast server REST call][Screenshot02] 

What this command is doing is testing a REST endpoint _"/k8s"_.
This will be used later on by Kubernetes to check the health of the
Hazelcast server, but it's just a REST endpoint so we can test
it too.

If everything is ok, we expect the result _"true"_.

*"k8s"* is a common abbreviation for Kubernetes, 3 characters instead
of 10, much less typing.

#### Check the Management Center

Refresh your Management Center web page, or log out and in. Look for the cluster named 
"k8s".

You should see that this cluster has 1 server member in the group:

![Image of Hazelcast management center member count][Screenshot03] 

### Hazelcast Client : `java -Dserver.port=8082 -jar the-client/target/the-client.jar`

Start the Hazelcast client using the standalone _Jar_ file build by Maven.

Again note the use of `-Dserver.port=8082` for the Tomcat web server. 8082 is a
just a choice that should be unique, as Hazelcast Management Center is
using 8080 and the Hazelcast Server is using 8081. This process is
therefore both a web *server* and a Hazelcast *client*.

#### Test the Hazelcast client

Try this command,

```
curl http://localhost:8082/k8s
```

This tests the _"/k8s"_ REST endpoint again. The Hazelcast client
implements this in the same way as the Hazelcast server, so that
Hazelcast can test the process for it being in a satisfactory
state.

Again, we can test it too, so we do. We expect the
result _"true"_ if the Hazelcast client is fit to use.

![Image of first Hazelcast client REST call][Screenshot04] 

#### Test the Hazelcast client again

Try this command,

```
curl http://localhost:8082
```
This tests the business logic, which returns a collection
of values stored in the map named _"hello"_.

What we get back is _"Hello World_" in 5 languages, like this:

![Image of second Hazelcast client REST call][Screenshot05] 

#### Check the Management Center again

On the Management Center, you should now see one server member
and one client connected.

![Image of Hazelcast management center client count][Screenshot06] 

### Shutdown

If we've got this far, then we know that the Hazelcast client and
Hazelcast server at least run fine on a bare metal deployment.
So if they don't work once we add Kubernetes we'll be more sure
of which step has gone wrong.

Shut down these Hazelcast Client, Hazelcast Server and Hazelcast
Management Center processes as we won't need them again, and we want
to avoid unnecessary load on your machine. Killing off the
three processes is the easiest way.

## Kubernetes concepts

It's time now for some concepts about Kubernetes, simplified but still valid.

### Docker

Firstly, Docker is not part of Kubernetes.

Docker is a "container" system much like a virtual machine. If you think of it
like a virtual machine you won't be far wrong. You won't be right either, but
it's close enough.

You bundle your application into a Docker "image", which you deploy to
a run time "container" that executes it. So the container is a bit
like a virtual machine, except it hosts only that one application.

### Kubernetes, Containers and Pods

Kubernetes is an eco-system for organising containers such as Docker.
It'll start them, stop them, and look after them on our behalf,
subject to a little guidance.

Kubernetes has many features, and we'll ignore most of them for this
demo.

One thing that needs a mention though is pods. Kubernetes groups containers
into collections it calls "pods". A Kubernetes set-up might have
many pods and each pod might have many containers. Each pod in this
demo will only have one container.

### Minikube

Minikube is an implementation of Kubernetes, for running on one
machine. This limits it's usefulness in terms of scaling to
many pods but it's great for getting started as you don't
need lots of hardware.

## Set-up

Now finally we need to get Kubernetes bits installed on your machine.

![Image of XXX][Screenshot07] 
![Image of XXX][Screenshot08] 
![Image of XXX][Screenshot09] 
![Image of XXX][Screenshot10] 
![Image of XXX][Screenshot11] 
![Image of XXX][Screenshot12] 
![Image of XXX][Screenshot13] 
![Image of XXX][Screenshot14] 
![Image of XXX][Screenshot15] 
![Image of XXX][Screenshot16] 
![Image of XXX][Screenshot17] 
![Image of XXX][Screenshot18] 
![Image of XXX][Screenshot19] 
![Image of XXX][Screenshot20] 
![Image of XXX][Screenshot21] 
![Image of XXX][Screenshot22] 
![Image of XXX][Screenshot23] 
![Image of XXX][Screenshot24] 
![Image of XXX][Screenshot25] 
![Image of XXX][Screenshot26] 
![Image of XXX][Screenshot27] 
![Image of XXX][Screenshot28] 
![Image of XXX][Screenshot29] 
![Image of XXX][Screenshot30] 

#### Windows - Docker shutdown

XXX Windows XXX

### Variations

If all has gone well on the above demo, you can always repeat it but
vary the number of Hazelcast server or Hazelcast clients that you
want. Just amend the `deployment.yaml` file in the obvious places, or
see the comments in it.

## Summary

Hazelcast works without Kubernetes, Hazelcast works with Kubernetes.

If you're using Docker for containerization, you'll find Kubernetes
the next sensible step.

What this example has tried to show is the detail involved. Most
of the time you won't care for the detail, so something like 
[Helm](https://helm.sh/) might be the next step to simplify
deployment.

You may wish to de-activate or uninstall any services installed to run this
demo (such as Docker), if you're not going to use them again.
