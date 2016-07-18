Hazelcast-Spring-TransactionManager-Example
====================================

This is a sample application to demonstrate `HazelcastTransactionManager`, an implementation of Spring's `PlatformTransactionManager`
to use Hazelcast's transactional data structures without boilerplate in `Transactional` methods.
This setup is configured to run in Server Mode where `HazelcastTransactionManager` is registered as the transaction manager.


Run Sample App
==============

**Transaction Manager Example**

mvn compile exec:java -Dexec.mainClass="com.hazelcast.spring.transaction.TransactionManagerExample"

or

Directly run the main method of `com.hazelcast.spring.transaction.TransactionManagerExample` inside your IDE.

