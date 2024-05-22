Hazelcast Code Samples Readme
=============================

The folder **/code-samples** in your package contains an extensive collection of code samples which you can use to learn how to use Hazelcast features. From distributed primitives to Service Provider Interface (SPI), you can see Hazelcast in action readily.

How to run
----------

If the sample has a README file, follow the instructions. A lot of samples have shell scripts like "start.sh", "start-member.sh" or "start-client.sh", use them to run the sample. If none of the previous options apply, use _java -cp target/classes:target/lib/\* <fully qualified name of the main class>_ to run the sample.

Included sample folders
-----------------------

*   **/clients** — Includes sample code that shows how to create a Hazelcast client, put a message on a queue by this client and receive that message by a Hazelcast node. Further how to set up client's near cache, deploy user code to member, communicate via REST or populate client statistics to member.
*   **/cluster** — cluster state managements and shutdown
*   **/cluster-split-brain-protection** — setting up cluster split brain protection programatically
*   **/cluster-split-brain-protection-xml** — setting up cluster split brain protection via XML configuration
*   **/cluster-state** — managing cluster state to frozen, passive and shutdown.
*   **/demo** — You can start multiple nodes and operate usual map operations using the code samples here.
*   **/distributed-collections** — Includes Distributed Queue, Bounded Queue, Listeners, Set, List, Queue store, Ringbuffer and Ringbuffer store code samples.
*   **/distributed-executor** — examples of distributed task execution e.g. executing on all member, just key owner, only lite nodes only, on specific member. Also examples of usage of futures and scheduled executions.
*   **/distributed-map** — An extensive code samples folder that includes many features of the Hazelcast distributed map in action.
*   **/distributed-primitives** — Includes Distributed AtomicLong, AtomicReference, CountDownLatch, IdGenerator, FlakeIdGenerator, CardinalityEstimator, Condition, Semaphore and Lock code samples.
*   **/distributed-topic** — Code samples to see publish/subscriber messaging model.
*   **/enterprise** — Includes socket and security interceptor, CPMap, HD store and hot restart code samples.
*   **/hazelcast-integration** — Code samples to show how you can integrate Hazelcast with Hibernate 2nd Level Cache and Spring. It also includes code samples for web session replication and resource adapter implementation.
*   **/jcache** — An extensive code samples folder for operations including creating a cache and writing entries to it, creating listeners and clients.
*   **/jet** — Code samples to demonstrate building stream and batch processing applications using Hazelcast's Jet engine.
*   **/jmx** — example of connecting to Hazelcast member via JMX.
*   **/json** — examples of JSON objects stored in Hazelcast.
*   **/learning-basics** — Code samples to show some Hazelcast basics like creating, configuring and destroying Hazelcast instances, configuring logging and Hazelcast configuration.
*   **/monitoring** — Includes code samples that show how to check if a Hazelcast cluster is safe to be shutdown.
*   **/near-cache** — Shows the benefits of Near Caches, local subsets of data that track the master copy in the grid. Further extends the _/clients_ examples.
*   **/network-configuration** — Shows what options Hazelcast configuration has regarding to network (e.g. partition grouping, multicast, ports, outbound ports, etc.)
*   **/osgi** — Demonstrates how to use Hazelcast's OSGI support
*   **/replicated-map** — Code samples to show how to get a replicated map and create an entry listener.
*   **/serialization** — Includes code samples that implement various serialization interfaces like DataSerializable, IdentifiedDataSerializable, Externalizable and Portable. It also has code samples to show how to plug a custom serializer using StreamSerializer and ByteArraySerializer interfaces and usage of Kryo and Protobuf serializers.
*   **/spi** — Includes code samples that create a simple counter application using Hazelcast’s Service Provider Interface (SPI).
*   **/sql** — Examples of using SQL directly, or via JDBC.
*   **/transactions** — Code samples showing how to use the TransactionalMap and TransactionalTask interfaces.

Included helper folders
-----------------------

*   **/checkstyle** — Java code style configuration
*   **/helper** — Shared utility classes and methods for samples