= Hazelcast 3.x Connector Sample

This example shows how to use the Hazelcast 3.x connector to transfer 
data between Hazelcast 3.x cluster and Hazelcast 5.x+ and back, or use
Hazelcast 3.x cluster for enrichment.

The purpose of the Hazelcast 3.x connector is to provide a way to 
evaluate Hazelcast 5 features on your existing data present in 
Hazelcast 3.x cluster.

In this demo we will do the following:
- Start Hazelcast 3.x cluster using the `Hz3MemberWithData` class -
this member represents an existing cluster with your data. The class 
loads tickers (stock symbols) on startup.

- Run a job `CopyMapFromHz3Example` which copies the data from a map in 
Hazelcast 3.x cluster into a map in 5.x cluster

- Start an empty Hazelcast 3.x cluster using `Hz3MemberNoData` class.

- Run a job `WriteBackMapToHz3Example` which copies data from a map in 
Hazelcast 5 cluster back to Hazelcast 3.x cluster

- Run a job `EnrichUsingMapHz3Example` which uses a map in Hazelcast 3.x 
cluster to enrich data in a stream job running in Hazelcast 5 cluster.

== Start Hazelcast 3.x cluster

Start Hazelcast 3.x member instance by running the main method of 
`com.hazelcast.samples.jet.hz3member.Hz3MemberWithData` class either
from your IDE or from the command line - in the 
"jet/hazelcast-3-connector/hazelcast-3-member" directory run 
the following:

```
mvn compile exec:java -Dexec.mainClass=com.hazelcast.samples.jet.hz3member.Hz3MemberWithData
```

Eventually you should see the following output in the console:

```text
Loaded 3170 tickers.
```

== Start Hazelcast 5 member

In a new terminal window start a new instance of Hazelcast 5.x 
using the full distribution:

```
$ bin/hz-start
```

== Build the job jar

In the `hazelcast-3-connector-jobs` run the following to build the jobs 
jar:

```
$ mvn package
```

Submit the job to the cluster by running the following from the 
Hazelcast distribution folder:

```
$ bin/hz-cli submit 
  --class com.hazelcast.samples.jet.hz3connector.CopyMapFromHz3Example 
  path/to/sample/target/hazelcast-3-connector-jobs-0.1-SNAPSHOT.jar 
  --source-map tickers 
  --target-map tickers
```

You should see the following message in the output:

```
After copying all the items from the source map the target map contains 3170 items in total.
```

== Start empty Hazelcast 3.x cluster

Stop the Hazelcast 3.x member and start it again, using the 
`Hz3MemberNoData` class this time:

```
mvn compile exec:java -Dexec.mainClass=com.hazelcast.samples.jet.hz3member.Hz3MemberNoData
```

You should see the following output:

```
Tickers map contains 0 tickers.
```

== Submit job writing back to Hazelcast 3.x cluster:

From the Hazelcast 5 distribution run the following:

```
$ bin/hz-cli submit 
  --class com.hazelcast.samples.jet.hz3connector.WriteBackMapToHz3Example 
  path/to/sample/target/hazelcast-3-connector-jobs-0.1-SNAPSHOT.jar 
  --source-map tickers 
  --target-map tickers
```

Eventually in Hazelcast 3.x member output you should see the following:

```
Tickers map contains 3170 tickers.
```

== Run enrichment job

From the Hazelcast 5.x distribution run the following:

```
$ bin/hz-cli submit 
  --class com.hazelcast.samples.jet.hz3connector.EnrichUsingMapFromHz3Example 
  path/to/sample/target/hazelcast-3-connector-jobs-0.1-SNAPSHOT.jar 
```

== Running jobs from IDE

You can use your IDE to run and debug your jobs. You need to import the 
maven project with `ide` profile.

Run the following command in the `hazelcast-3-connector-jobs` module: 

```
mvn process-resources
```

This command copies required Hazelcast 3.x jars to correct location.

Now you can run the job's class main method directly. 
Note that it expects the working directory is set to module directory.


