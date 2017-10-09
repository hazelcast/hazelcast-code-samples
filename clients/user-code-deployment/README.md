Client user code deployment code sample consist of two different mvn projects. This is to be able to demonstrate class deployment from client to server.  

1. Run hazelcast member with user code deployment enabled. Note that, it does not contain the `IncrementingEntryProcessor` class that will be deployed from client.

```
cd user-code-deployment-member
mvn exec:java -Dexec.mainClass="Member"
```

2. Run hazelcast client with user code deployment enabled. Client will first initialize the map with entries that have 0 as values. Then it will deploy and run `IncrementingEntryProcessor` on all entries. All values of map should be incremented to 1.

```
cd user-code-deployment-client
mvn compile exec:exec
```

