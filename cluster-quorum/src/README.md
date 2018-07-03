# Split-brain protection

This code sample shows how to configure split-brain protection with the default or with a custom `QuorumFunction` implementation.

## Build and run the samples

Build this sample with Maven.
```
mvn clean install
```

The `ClusterQuorum` sample demonstrates configuration of built-in split-brain protection based on:
  - member count as observed by the local member's membership manager (available since Hazelcast 3.5)
  - probabilistic quorum function (since Hazelcast 3.10)
  - recent activity of members (since Hazelcast 3.10)

Separate maven profiles execute the sample, configured with the corresponding built-in quorum function each time:
```bash
mvn exec:java -Pmember-count
mvn exec:java -Pprobabilistic
mvn exec:java -Precently-active
```

The `CustomClusterQuorum` sample, which demonstrates implementation and configuration of a custom quorum function, can be executed with:

```bash
mvn exec:java -Pcustom-quorum-function
```
