# Hazelcast EntryListener Hand Off

## Introduction

Hazelcast [EntryListener](http://docs.hazelcast.org/docs/latest/manual/html/map-entrylistener.html) are used to react to events that occur on Maps. An EntryListener is a callback interface
that a developer can implement to execute their code in reaction to a Map Event, such as Update, Add, Delete.

Internally EntryListeners are called via the internal hazelcast event thread pool.  The event thread pool is described [here](http://docs.hazelcast.org/docs/latest/manual/html/threadingevent.html)

In general it is considered bad practice to have long running code hanging off these threads and inside the EntryListeners.  If your code runs for too long or gets into a deadlock situation then
events that are waiting behind will delay or in the worst case eventually get lost as there is no more room on the event queue.

## EntryListener Hand Off

This project is a framework that introduces the concept of handing off event processing from the internal hazelcast event threads and onto a thread pool that is more clearly controlled by the developer.  In a nutshell
it does exactly as the name describes.  It takes events from the event callback and then places them into a new thread pool.

## EntryEventService

The Framework resides around the central concept of an [EntryEventService](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/EntryEventService.java).  The EntryEventService is responsible for processing the [EntryEvent](http://docs.hazelcast.org/docs/latest/javadoc/) that are passed to EntryListeners.

The EntryEventService has one method called process.  This method takes the EntryEvent passed by Hazelcast and a [CompletionListener](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/listener/CompletionListener.java)

The EntryEventService is responsible for examining the EntryEvent and processing it appropriately.  Some implementations may make use of the [EntryEventTypeProcessorFactory](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/processors/EntryEventTypeProcessorFactory.java).  This Factory provides an [EntryEventProcessor](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/processors/EntryEventProcessor.java) per EntryEventType.

<img src="/src/main/diagrams/EntryEventService.jpg" alt="EntryEventService"/>

## ThreadPoolEntryEventService

There is currently one implementation of an EntryEventService, called the [ThreadPoolEntryEventService](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/ThreadPoolEntryEventService.java), it provide the following features :-

 1. ThreadPoolEntryEventService provides a striped set of ThreadPoolExecutors that guarantees execution order by key.
 2. A Warning Service to inform of long running EntryEventTypeProcessors
 3. CompletionService callback to inform of failed/completed processes
 4. Getter to retrieve the striped queues of waiting EntryEvents

## EntryEventServiceDelegate

 [EntryEventServiceDelegate](/src/main/java/com/craftedbytes/hazelcast/entryevent/service/EntryEventServiceDelegate.java) is a Utility class takes care of handing off between the EntryListener and the EntryEventService.  You can configure this class to be the EntryListener on your maps.

## Spring based example

There is an example that is started by running [HazelcastBookCountOnWordsExample](/src/main/java/com/craftedbytes/hazelcast/entryevent/examples/HazelcastBookCountOnWordsExample.java)

This example bootstraps the Hazelcast Cluster Member by Spring config found at [application-contaxt.xml](/src/main/resources/application-context.xml), it then loads 3 public domain books [AdventuresOfHuckleberryFinn.txt,Metamorphosis.txt,Ulysses.txt] into an IMap.  The IMap is keyed by title and the value is the text of the book.
There is an EntryListener attached to the map and when each book is put into the map it invokes a WordCountEntryEventProcessor via a ThreadPoolEntryEventService






