# JSON Grid

Hello JSON, hello YAML and goodbye XML!

This is an example showing the use of YAML configuration, and populating
a Hazelcast grid with data in JSON format that has been extracted from
a traditional relation database. 

So the data goes from tables into "_NoSQL_" format.

## Hello YAML, goodbye XML

Hazelcast has a number of ways to be configured.

Sensible defaults mean you don't have to bother with most of the
configuration. But you can do configuration from Java, or from
from XML. From Hazelcast 3.12 onwards, YAML is also possible.

So to configure Hazelcast from YAML, you might have a file named
`hazelcast.yml` containing this:

```
hazelcast:
  group:
    name: 'jsongrid'
  management-center:
    enabled: true
    url: 'http://localhost:8080/hazelcast-mancenter'
  network:
    port:
      port: 9701
    join:
      multicast:
        enabled: false
      tcp-ip:
        enabled: true
        member-list:
          - 127.0.0.1:9701
```

This would replace old XML style,

```
<hazelcast>
     <group>
          <name>jsongrid</name>
     </group>

     <management-center enabled="true">http://localhost:8080/hazelcast-mancenter</management-center>
     <network>
          <port>9701</port>
          <join>
               <multicast enabled="false"/>
               <tcp-ip enabled="true">
                    <member-list>
                         <member>127.0.0.1:9701</member>
                    </member-list>
               </tcp-ip>
          </join>
     </network>
```

Both achieve the same thing, it's just a question of preference.

## Hello JSON

The next thing want to do is have easier handling of JSON.

Hazelcast has always supported strings as data keys and data values,
and we can easily store JSON in this format. Really, JSON is just
a string with a loose type system applied. There is no strict format
checking, it's free-form, which is the benefit (or drawback!) of JSON.

However, for efficient querying of JSON we need Hazelcast to *understand*
that the data is really a JSON object.


## The example - _jsongrid_

This example is a Hazelcast grid holding JSON data, named "_jsongrid_".

The example here consists of four modules. Build in the usual Maven
way with

```
mvn clean install
```

What this will build is four modules:

* jsongrid-database A [HSQL](http://hsqldb.org/) database
* jsongrid-database-tester A JDBC routine to prove the database is valid.
* jsongrid-server A Hazelcast server process, one of at least one in the grid
* jsongrid-client-java A client of this grid, that happens to be written in Java too

## Motivation

The whole motivation here is to see how much can be done without considering
Java.

The database is a relational database, typical of many places. Here we code it
as [HSQL](http://hsqldb.org/) but other choices such as MySql, Postgres and
Oracle should be easy replacements.

The Hazelcast grid reads from this relational database to store the same
content in memory as JSON objects. It does this in a generic way, so there
is no need to define the data model, the code deduces it.

The Hazelcast client queries data in the Hazelcast grid, running a query
against fields in the JSON objects.

So the basic idea here is around JSON. The grid deduces how to get JSON in
from an external source (a relational database), and the client queries this
data without caring where it came from. 

From a developer or architects viewpoint, we can be _querying_ *JSON* data at _memory
speed_ that *somehow* came from a relational database, without bothering too much
about the details. 

### The relational database (RDBMS)

The first module to look at is the relation database itself.

In most organisations there are relational databases.

In this example we run a [HSQL](http://hsqldb.org/) database,
but you can substitute MySql, Oracle, Postgres, whatever you
like. Connectivity is via JPA over JDBC, so as long as the
database follows this standard it won't matter.

Run a database instance using this command:

```shell-script
java -jar jsongrid-database/target/jsongrid-database.jar
```

This module will take two table defiiations (in `json-database/src/main/resources/schema.sql`)
and create the according tables. Data datas comes is loaded also
(from `json-database/src/main/resources/data.sql`).

The volumes here are 45 rows for the `POTUS` table and 48 rows for
the `VPOTUS` table. So we can easily afford to load everything into
memory rather than a selection, and 93 data records will easily fit
in one Hazelcast server JVM. We can and should run more servers, but
one would do if resources are really constrained.

### Testdata

The test data here is a list of the
[Presidents of the United States](https://en.wikipedia.org/wiki/List_of_Presidents_of_the_United_States)
and
[Vice-Presidents of the United States](https://en.wikipedia.org/wiki/List_of_Vice_Presidents_of_the_United_States)

Each is a row in a table. Some columns may be null, such as middle name. Other
columns may not be null, such as the date of taking office.

Note, other politicians are available :-)

After a successful build, the database is run with:

```
java -jar jsongrid-database/target/jsongrid-database.jar
```

The database is a silent resource. We don't really know what is in it unless an
error message appears, so we need some sort of way to prove it is correctly
loaded. This is where the `jsongrid-database-tester` comes in.

### Independent database tester

Just to prove the database is working properly, a database tester
is included. This is a standalone routine that connects to the
database, does a query and outputs the results. It contains nothing
to do with Hazelcast.

Run this as:

```
java -jar jsongrid-database/target/jsongrid-database.jar
```

What this does is dump the `potus` and `vpotus` tables to the screen.
Output should include this:

```
SELECT COUNT(*) FROM potus
1:C1==45
[1 row]
~~~~~~~~~~~~~~~~~~~~~~~~~~
~~~~~~~~~~~~~~~~~~~~~~~~~~
SELECT COUNT(*) FROM vpotus
1:C1==48
[1 row]
~~~~~~~~~~~~~~~~~~~~~~~~~~
```

At the time of writing there have been 45 presidents and 48 vice-presidents,
so that's how many rows should be found.

### The Hazelcast grid

This needs the database to be running.

Start a Hazelcast grid member using 

```
java -jar jsongrid-server/target/jsongrid-server.jar
```

One is enough for this data volume 93 records! Start more servers if you like.

#### The logic

The key part of the Hazelcast server is the map loader.

The server queries all database tables in the database. It should find "_POTUS_" and
"_VPOTUS_" tables as that's what we insert into the database.

For each of these, the server will attempt to access an map in Hazelcast with the
same name, which will trigger a map loader.

The map loader will find all rows in the table, and convert each to JSON to store
in Hazelcast.

So effectively every row in every table in the database becomes an entry in a Hazelcast
map, in JSON format.

Hazelcast doesn't need to know the format of the table, it can deduce it from the information
returned.

### The JSON client

This needs one or more members of the Hazelcast grid to be running.

The demonstrator of value here is a routine that connects to the Hazelcast grid and
queries the data.

In this example, it's written in Java and run with:

```
java -jar jsongrid-client-java/target/jsongrid-client-java.jar
```

If you run it, you should get query output incuding:

```
IMap 'POTUS', size 45
IMap 'VPOTUS', size 48
- - - - - - - - - - - - - -
TOOKOFFICE LIKE '%-03-04'
{ "ID" : "2", "NAME" : "Thomas Jefferson", "TOOKOFFICE" : "1797-03-04", "LEFTOFFICE" : "1801-03-04" }
{ "ID" : "27", "NAME" : "James S Sherman", "TOOKOFFICE" : "1909-03-04", "LEFTOFFICE" : "1912-10-30" }
```

The query is expressed as a string, `TOOKOFFICE LIKE '%-03-04'`.

The results come back as JSON, `{ "ID" : "2", "NAME" : "Thomas Jefferson", "TOOKOFFICE" : "1797-03-04", "LEFTOFFICE" : "1801-03-04" }`.

Java is just a choice. You could do this with GoLang, C#, C++, Node.js, etc. Any of the client
languages, whatever suits you best.

#### Query 1 : `John`

The query here is looking for "_John_" as part of the US president's name.

Specifically `FIRSTNAME = 'John' OR MIDDLENAME1 = 'John' OR MIDDLENAME2 = 'John' OR LASTNAME = 'John'`.

This will match against this JSON, amongst others

```
{ "ID" : "45", 
  "FIRSTNAME" : "Donald",
  "MIDDLENAME1" : "John",
  "MIDDLENAME2" : "null", 
  "LASTNAME" : "Trump", 
  "TOOKOFFICE" : "2017-01-20", 
  "LEFTOFFICE" : "null", 
  "AKA" : "null" }
```

The 45th president is _Donald Trump_, his first middle name is _John_.. 

You should get six matches in total:

```
{ "ID" : "2", "FIRSTNAME" : "John", "MIDDLENAME1" : "null", "MIDDLENAME2" : "null", "LASTNAME" : "Adams", "TOOKOFFICE" : "1797-03-04", "LEFTOFFICE" : "1801-03-04", "AKA" : "null" }
{ "ID" : "45", "FIRSTNAME" : "Donald", "MIDDLENAME1" : "John", "MIDDLENAME2" : "null", "LASTNAME" : "Trump", "TOOKOFFICE" : "2017-01-20", "LEFTOFFICE" : "null", "AKA" : "null" }
{ "ID" : "6", "FIRSTNAME" : "John", "MIDDLENAME1" : "Quincy", "MIDDLENAME2" : "null", "LASTNAME" : "Adams", "TOOKOFFICE" : "1825-03-04", "LEFTOFFICE" : "1829-03-04", "AKA" : "null" }
{ "ID" : "30", "FIRSTNAME" : "John", "MIDDLENAME1" : "Calvin", "MIDDLENAME2" : "null", "LASTNAME" : "Coolidge", "TOOKOFFICE" : "1923-08-02", "LEFTOFFICE" : "1929-03-04", "AKA" : "Calvin" }
{ "ID" : "10", "FIRSTNAME" : "John", "MIDDLENAME1" : "null", "MIDDLENAME2" : "null", "LASTNAME" : "Tyler", "TOOKOFFICE" : "1841-04-04", "LEFTOFFICE" : "1845-03-04", "AKA" : "null" }
{ "ID" : "35", "FIRSTNAME" : "John", "MIDDLENAME1" : "Fitzgerald", "MIDDLENAME2" : "null", "LASTNAME" : "Kennedy", "TOOKOFFICE" : "1961-01-20", "LEFTOFFICE" : "1963-11-22", "AKA" : "Jack" }
```

#### Query 2 : `4th March`

Until the ratification of the 12th amendment to the constitution, inauguration day for the
president was usually the 4th of March. And, indirectly for the vice president too.

We run this question to see which vice-presidents took office on the 4th March.

```
TOOKOFFICE LIKE '%-03-04'
```

This looks for records where the date of taking office (the `TOOKOFFICE` column) matches
the pattern.

There are 29 results, such as

```
{ "ID" : "2", "NAME" : "Thomas Jefferson", "TOOKOFFICE" : "1797-03-04", "LEFTOFFICE" : "1801-03-04" }
{ "ID" : "27", "NAME" : "James S Sherman", "TOOKOFFICE" : "1909-03-04", "LEFTOFFICE" : "1912-10-30" }
{ "ID" : "14", "NAME" : "John C Breckinridge", "TOOKOFFICE" : "1857-03-04", "LEFTOFFICE" : "1861-03-04" }
{ "ID" : "6", "NAME" : "Daniel D Tompkins", "TOOKOFFICE" : "1817-03-04", "LEFTOFFICE" : "1825-03-04" }
{ "ID" : "19", "NAME" : "William A Wheeler", "TOOKOFFICE" : "1877-03-04", "LEFTOFFICE" : "1881-03-04" }
```

Note that query results aren't ordered, so you might get them a different way round.
But essentially this shows

```
{ "ID" : "2",
  "NAME" : "Thomas Jefferson", 
  "TOOKOFFICE" : "1797-03-04", 
  "LEFTOFFICE" : "1801-03-04" }
```

Thomas Jefferson was the 2nd vice-president of the United States (and 3rd president). He took
office as vice-president on the 4th of March 1797.

## Conclusion

Hazelcast can be configured from code, from XML, from Spring, and now from YAML. Pick what suits
you, it's not exactly important.

JSON can be queried efficiently, the grid knows how to parse JSON objects. You can search for
records in a memory-based data-grid (Hazelcast!) faster than a disk-based system.

If you have a legacy store, you can have an optimized data loader that understands how to
translate the data model as relation tables into JSON. Or you can do as we do here, a generic
routine that decudes what to do. Pick which fits your need.
