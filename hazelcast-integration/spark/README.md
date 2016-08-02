# Hazelcast Spark Connector Example

This repository contains the following examples:

-   Word count example: Reads a file from the file system, counts the number of occurrences of each word with Apache Spark and saves the results to a Hazelcast map.
-   Average age example: Reads the user records from a Hazelcast map, then calculates the average age of users with Apache Spark.

## Building the project

To build the project, run the following command:

```
mvn clean package
```

## Running the Example

To run the examples, you need to have a running Hazelcast cluster. To start a Hazelcast instance run the following command:

```
sh ./start-member.sh
```

After that, you can run the examples by running the appropriate shell script for that example.

To see the word count example in action, run the following command:

```
sh ./word-count.sh
```

For the average age example, run the following command:

```
sh ./average-age.sh
```
