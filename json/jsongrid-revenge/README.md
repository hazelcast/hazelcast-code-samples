# JSON Grid Revenge

XXX Revenge - Reverse Engineering
XXX No XML - HZ config
XXX No XML - Hibernate

## The relational database (RDBMS)

In most organisations there are relational databases.

In this example we run a [HSQL](http://hsqldb.org/) database,
but you can substitute MySql, Oracle, Postgres, whatever you
like. Connectivity is via JPA over JDBC, so as long as the
database follows this standard it won't matter.

Run a database instance using this command:

```shell-script
java -jar jsongrid-database/target/jsongrid-database.jar
```

## Testdata

The test data here is a list of the Presidents of the United
States, taken from
[wikipedia](https://en.wikipedia.org/wiki/List_of_Presidents_of_the_United_States)

Each is a row in a table. Some columns may be null, such as middle name. Other
columns may not be null, such as the date of taking office.

Note, other politicians are available :-)

## Independent database tester

Just to prove the database is working properly, a database tester
is included. This is a standalone routine that connects to the
database, does a query and outputs the results. It contains nothing
to do with Hazelcast.

Run this as:

```shell-script
java -jar jsongrid-database/target/jsongrid-database.jar
```

What this does is dump the `potus_t` table to the screen.

##