# Hazelcast Spark Connector Example

This repository contains two examples, which are :

-   Word Count Example, reads a file from filesystem, counts the number of occurrences of each word with Apache Spark and saves the results to a Hazelcast map.
-   Average age example, reads the user records from a Hazelcast map, then calculates the average age of users with Apache Spark.

## Building the project

To build the project, run the command below:

```
mvn clean package
```

## Running the Example

To run examples you've need to have a running Hazelcast cluster. To start a Hazelcast instance just run the command below.

```
sh ./start-member.sh
```

After that, to you can run the examples by running the appropriate shell script for that example.

To run Word Count example execute the command below,
```
sh ./word-count.sh
```

To run Average Age example execute the command below,

```
sh ./average-age.sh
```
