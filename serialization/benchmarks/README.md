# Comparing serialization options

This guide will help you decide which serialization mechanism is best for your data.

There is no generic "_best_" answer. It will depend on your data.

## What You’ll Learn

In this guide you'll learn

* sizes using different serialization strategies
* timings using different serialization strategies
* why these won't be the results you get
* why you need to be very careful with benchmarking

## Prerequisites

None. This guide is more of a tutorial explanation.

You can run the benchmark on your machine, as an optional step. Build with Maven and run:

```
java -jar runner/target/serialization-benchmarks-runner-1.0.jar
```

## The data model

This data model is used. A `Person` object may contain a `Passport`.

```
class Person {
    String firstName;
    String lastName;
    Passport passport;
```

and

```
class Passport {
    String expiryDate;
    String issuingCountry;
    long issuingDate;
```

#### Observations on this data model

Text features more than numerics.

Names are comparatively short as text goes, but the average length shuoldn't be too hard to find.

### The test data

To produce measurements, test data is used.

100,000 `Person` objects are created, with a random selection of first and last names, and a 50% chance of
having a `Passport` or not.

## What factors matter

Before we get started, we need to consider what factors matter.

* Size is an obvious factor, but don't let it obscure the others. If the data model serializes to half the
size with one option instead of another, you can store twice as much data. But if don't have a lot of data,
the storage need is irrelevant.

* Network behaviour is linked to size. When you send or retrieve data, it passes across the network in blocks.
A large object may need more blocks so take longer. But the block size can be tuned so perhaps all serialization
mechanisms produce an object that fits in one block and so network time is constant. Or you use a
https://docs.hazelcast.com/hazelcast/latest/cluster-performance/best-practices#near-cache[near-cache] and network transfer rarely happens.

* Serialization is the process of turning the object into bytes, and can be CPU intensive.
** In client-server topology this occurs on the client when sending the data, and on the server if writing the data in-situ. Client machines often have less CPUs than server host machines.
** In embedded topology, it's servers only so the allocation of CPUs is a more straightfoward question.

* Deserialization is the reverse of serialization, but tends to require less from the CPUs.
** In client-server topology this occurs on the client when reading the data, and on the server if reading the data in-situ. Client machines often have less CPUs than server host machines.

* Querying indirectly invokes deserialization. Data held as serializaed needs some form of deserialization
to determine if it is a query match. If querying is frequent fast deserialization will be beneficial,
and _partial deserialization_ (of only the queried fields) is better still.

* Inter-operability means the use of Hazelcast with one or more other languages. If a .NET system is going
to use data in Hazelcast, a Java only serialization mechanism won't work.

## 3 styles of serialization

There are 3 styles of serialization, with variants for each.

* Java standard
* Hazelcast optimized
* External libaries

In the next sections we'll look at each. For external libraries, there are many available so we will use
three popular ones (but there are others and they may be better).

- Avro, https://avro.apache.org
- Kryo, https://github.com/EsotericSoftware/kryo
- Protobuf, https://developers.google.com/protocol-buffers

## Java standard

If you need inter-operability with other languages, now or in the future, forget about these and skip to the next section.

### Serializable

This is the easiest option. Add `implements Serializable` to the classes on your domain model and you're done.

You don't need to write the serialization logic, Java will deduce it using reflection.

Easy is good in terms of productivity and prototyping. You don't write code, so don't need to write tests. You're pretty much done.

So something like this for `Person`, and the same for `Pasport`.

```
class Person implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    String firstName;
    String lastName;
    Passport passport;
```

The downside is that reflection is slow and produces a serialized object that contains a lot of metadata.

Benchmarking shows this to be the worst option in all categories, but you don't have to write the serialization logic.

#### Opinion on `java.io.Serializable`

It's good for prototyping.

It's fine for data with low volumes, nothing wrong with it.

It's necessary if your data model needs `Serializable` for other things, like Hibernate or JPA `@Entity`.

### Externalizable

For `Externalizable` you provide methods that do most of the serialization and deserialization work.

One method to serialize (`writeExternal()`) and one to deserialize (`readExternal()`). Java still does
some reflection, but not as much as for `java.io.Serializable`.

While it's not a lot of coding, it's now coding so the chance for coding errors. Unit tests are
strongly recommended.

```
class Person implements java.io.Externalizable {

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.firstName);
        out.writeUTF(this.lastName);
        out.writeObject(this.passport);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.firstName = in.readUTF();
        this.lastName = in.readUTF();
        this.passport = (Passport) in.readObject();
    }
```

#### Opinion on `java.io.Externalizable`

It's good if you don't want your domain model to have dependencies on Hazelcast or external software.

## Hazelcast optimized

Hazelcast provides 5 mechanisms for serialization, with internal optimizations that don't affect your
code.

### DataSerializable

This is similar to Java standard `Externalizable`. You provide a method to serialize
and deserialize each class.

There's still some Java internals behind the scenes, so this is still a Java only mechanism.

```
class Person implements com.hazelcast.nio.serialization.DataSerializable {

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(this.firstName);
        out.writeString(this.lastName);
        out.writeObject(this.passport);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.passport = in.readObject();
    }
```

#### Opinion on `com.hazelcast.nio.serialization.DataSerializable`

It's ok. Hazelcast optimizations make `DataSerializable` better than Java's `Externalizable` for the same amount of coding.

If thinking `DataSerializable`, why not use `IdentifiedDataSerializable`.

### IdentifiedDataSerializable

`IdentifiedDataSerializable` is a successor to `DataSerializable`.

The `readData()` and `writeData()` methods are the same, but now you also provide a factory Id and a class Id,
and a factory class.

```
class Person implements com.hazelcast.nio.serialization.IdentifiedDataSerializable {

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 1;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(this.firstName);
        out.writeString(this.lastName);
        out.writeObject(this.passport);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.passport = in.readObject();
    }
```

and

```
class MyIdentifiedDataSerializableFactory implements com.hazelcast.nio.serialization.DataSerializableFactory {

    @Override
    public IdentifiedDataSerializable create(int classId) {
        if (classId == 1) {
            return new Person();
        }
        if (classId == 2) {
            return new Passport();
        }
        return null;
    }
```

With the previous three serialization mechanisms, the first item in the byte array is the name of the class,
so the receiver knows what it is received.

If the class is "_com.something.something.Person_" that's 30 bytes. With `IdentifiedDataSerializable`, the factory Id and class Id are sent instead, 4 bytes each. 8 bytes are less than 30.

As a bonus, since the class name isn't mentioned this works for other languages that don't follow Java class naming.
Anywhere the bytes are deserialized needs an implementation of the factory to build the object in whatever language
happens to be in use.

This mechanism is inter-operable. You can have a Java factory, a .NET factory, a GoLang factory, all the languages -- however the factories need to have matching logic.

#### Opinion on `com.hazelcast.nio.serialization.IdentifiedDataSerializable`

This is what Hazelcast uses internally for the data it passes about for cluster administration.

It's a good choice if querying isn't the overriding use case.

### Portable

`Portable` is similar to `IdentifiedDataSerializable` with it's need for a factory Id, a class Id and a
factory. 

```
class PersonPortable implements com.hazelcast.nio.serialization.Portable {

    @Override
    public int getFactoryId() {
        return 2;
    }

    @Override
    public int getClassId() {
        return 1;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("firstName", this.firstName);
        writer.writeString("lastName", this.lastName);
        if (this.passport == null) {
            writer.writeNullPortable("passport", 2, 2);
        } else {
            writer.writePortable("passport", this.passport);
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.firstName = reader.readString("firstName");
        this.lastName = reader.readString("lastName");
        this.passport = reader.readPortable("passport");
    }
```

Again, this is inter-operable with other languages, so a "_not just Java_" choice.

The key difference from `IdentifiedDataSerializable` is that when each field is written to the byte
array it is prefixed by its field name.

The byte array contains meta-data. This makes it larger, but means individual fields can be found in the byte
array.

So when querying data with "_SELECT * FROM person WHERE firstName = 'John'", only the `firstName` field needs
deserialized, not the whole object. This makes querying faster.

#### Opinion on `com.hazelcast.nio.serialization.Portable`

Good for query speed.

`Portable` is better than `IdentifiedDataSerializable` when querying is more frequent, but you won't necessarily
know this in advance and it may change over time.

### VersionedPortable

`VersionedPortable` is an extension to `Portable` for object versioning. You can write data to the grid with
version 1, and read it back with version 2. This is useful if your data model evolves over time
but you don't want to delete all the data in your grid when the data model changes.

It adds once extra method, for the version. An `int` is 4 bytes, so the serialized object is 4
bytes bigger.

```
public class PersonVersionedPortable implements com.hazelcast.nio.serialization.VersionedPortable {
    }

    @Override
    public int getClassVersion() {
        return MyConstants.PERSON_VERSIONED_PORTABLE_CLASS_VERSION;
    }
```

#### Opinion on `com.hazelcast.nio.serialization.VersionedPortable`

Consideration between `Portable` and `VersionedPortable` is more about the bigger issue of versioning.

If you need strong typing, an evolving data model yet cannot afford to discard
and recreate data it's one to go for.

### HazelcastJsonValue

`HazelcastJsonValue` is an inbuilt type in Hazelcast that accepts a String that contains
JSON. You might use it as follows:

```
    stringBuilder = new StringBuilder();

    stringBuilder.append("{");
    stringBuilder.append("\"firstName\":\"" + firstName + "\",");
    stringBuilder.append("\"lastName\":\"" + lastName + "\"");
    stringBuilder.append("}");

    HazelcastJsonValue person = new HazelcastJsonValue(stringBuilder.toString());
```

Internally Hazelcast inspects the structure of the first few JSON objects
it is given to deduce how to optimally query this data.

It is inter-operable across the client languages.

Whereas `VersionedPortable` above enforces strict typing but allows two objects
to have different definitions, JSON does the same with loose typing.

#### Opinion on `com.hazelcast.core.HazelcastJsonValue`

JSON is very popular, and has other uses. It's a good choice as part of
a wider ecosystem.

Web clients for example work with JSON. If that's the same format for storing
in Hazelcast, then the convenience is a key consideration.

## External libraries

For the purposes of benchmarking, we shall use three external libraries:

- Avro, https://avro.apache.org
- Kryo, https://github.com/EsotericSoftware/kryo
- Protobuf, https://developers.google.com/protocol-buffers

These are commonly used, which is why the were selected here. But others exist or are being
developed, and may be more suitable. It's worth doing your own research.

### Avro, https://avro.apache.org

Avro works from a record definition file, and generates the Java class
and serialization logic.

```
 {
  "type": "record",
  "name": "PersonAvro",
  "fields": [
     {"name": "firstName", "type": "string"},
     {"name": "lastName", "type": "string"},
     {"name": "passport", "type": ["null", "PassportAvro"], "default": null}
  ]
 }
```

It can generate other languages from the same definition, which makes
it easy to use for inter-operability.

#### Opinion on https://avro.apache.org

A popular choice, and Apache backed.

If you're working with Kafka, you may well come across it. So if you were to use it
for Hazelcast also, it's less complication in your tech stack.

### Kryo, https://github.com/EsotericSoftware/kryo

Kryo uses a helper class, which you write, for serialization.

The data model has no serialization logic, which could be useful if the
data model is controlled by another team or is somehow external to
your application.

The helper class might be coded:

```
public class PersonKryoSerializer implements StreamSerializer<PersonKryo> {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(PassportKryo.class);
            kryo.register(PersonKryo.class);
            return kryo;
        }
    };

    @Override
    public void write(ObjectDataOutput out, PersonKryo object) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Output output = new Output((OutputStream) out);
        kryo.writeObject(output, object);
        output.flush();
    }

    @Override
    public PersonKryo read(ObjectDataInput in) throws IOException {
        Input input = new Input((InputStream) in);
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        return kryo.readObject(input, PersonKryo.class);
    }
```

The key steps here are the initial `kryo.register(PersonKryo.class);` where the
class is passed to Kryo. Kryo does all the work here of figuring out
how to serialize the object, but this is mostly a one-off operation.

The `write()` and `read()` methods are comparatively simple to code, using
the Kryo object instantiation.

#### Opinion on https://github.com/EsotericSoftware/kryo

Kryo is fast, but support for other languages is limited.

If you are Java only, it may be a contender. 

The data model and serialization are separated, which may be favorable.

### Protobuf, https://developers.google.com/protocol-buffers

Finally, Protobuf from Google.

This takes a similar approach to Avro. A text file defines the data
record, and Java code (and other languages) are generated from it.

```
message PersonProtobuf {
  string firstName = 1;
  string lastName = 2;
  PassportProtobuf passport = 3;
}
```

Although it generates the Java code, it won't generate the serializer
for Hazelcast. However, this is easy enough:

```
public class PersonProtobufSerializer implements ByteArraySerializer<PersonProtobuf> {

    @Override
    public byte[] write(PersonProtobuf object) throws IOException {
        return object.toByteArray();
    }

    @Override
    public PersonProtobuf read(byte[] buffer) throws IOException {
        return PersonProtobuf.parseFrom(buffer);
    }
```

#### Opinion on https://developers.google.com/protocol-buffers

Protobuf is another common format, and is used in conjunction with
https://grpc.io/[gRPC].

So it's a good choice if you're going to use gRPC.

## The measurements 

Firstly, sizing.

The benchmark sizes 100,000 objects and reports the size of the first object.

So for `java.io.Serializable` this data has an average object size of 322 bytes but
the first object is 402. It's important to understand the spread of sizes
in the data.

```
Sizes for 100,000 records.
                                        java.io.Serializable :   32,252,640 bytes :   first object is 402 bytes
                                      java.io.Externalizable :   15,599,682 bytes :   first object is 210 bytes
            com.hazelcast.nio.serialization.DataSerializable :   15,599,218 bytes :   first object is 206 bytes
  com.hazelcast.nio.serialization.IdentifiedDataSerializable :    6,045,448 bytes :   first object is  78 bytes
                    com.hazelcast.nio.serialization.Portable :   15,799,102 bytes :   first object is 207 bytes
           com.hazelcast.nio.serialization.VersionedPortable :   15,799,102 bytes :   first object is 207 bytes
                       com.hazelcast.core.HazelcastJsonValue :    9,948,464 bytes :   first object is 143 bytes
                                     https://avro.apache.org :    3,394,462 bytes :   first object is  43 bytes
                    https://github.com/EsotericSoftware/kryo :    3,094,346 bytes :   first object is  39 bytes
              https://developers.google.com/protocol-buffers :    4,144,752 bytes :   first object is  53 bytes
```

Secondly, timings.

Sizes are reliable to benchmark. Timings are impacted by other things
going on on the machine, paging, swapping, etc. You should run several
runs to mitigate the effect of this.

For `java.io.Serializable` again, it's 33 seconds for serialization and 11 for
deserialization. Deserialization is easier.

```
Benchmark                                                           (kindStr)  Mode  Cnt      Score      Error  Units
Timer.deserialize                                        java.io.Serializable  avgt    3  33655.348 ± 6089.926  ms/op
Timer.deserialize                                      java.io.Externalizable  avgt    3   8494.364 ±  212.126  ms/op
Timer.deserialize            com.hazelcast.nio.serialization.DataSerializable  avgt    3   3563.776 ±  135.823  ms/op
Timer.deserialize  com.hazelcast.nio.serialization.IdentifiedDataSerializable  avgt    3   1861.914 ± 1042.587  ms/op
Timer.deserialize                    com.hazelcast.nio.serialization.Portable  avgt    3   3338.154 ± 5395.007  ms/op
Timer.deserialize           com.hazelcast.nio.serialization.VersionedPortable  avgt    3   2920.613 ±  461.255  ms/op
Timer.deserialize                       com.hazelcast.core.HazelcastJsonValue  avgt    3    918.050 ±  677.488  ms/op
Timer.deserialize                                     https://avro.apache.org  avgt    3   8859.665 ± 6992.005  ms/op
Timer.deserialize                    https://github.com/EsotericSoftware/kryo  avgt    3   4887.225 ± 1740.484  ms/op
Timer.deserialize              https://developers.google.com/protocol-buffers  avgt    3   2267.215 ±  365.659  ms/op
Timer.serialize                                          java.io.Serializable  avgt    3  11748.139 ± 4072.223  ms/op
Timer.serialize                                        java.io.Externalizable  avgt    3   4965.973 ±  758.891  ms/op
Timer.serialize              com.hazelcast.nio.serialization.DataSerializable  avgt    3   2312.038 ±  128.996  ms/op
Timer.serialize    com.hazelcast.nio.serialization.IdentifiedDataSerializable  avgt    3   1920.426 ±  129.446  ms/op
Timer.serialize                      com.hazelcast.nio.serialization.Portable  avgt    3   4121.768 ±   76.325  ms/op
Timer.serialize             com.hazelcast.nio.serialization.VersionedPortable  avgt    3   3735.342 ±   65.550  ms/op
Timer.serialize                         com.hazelcast.core.HazelcastJsonValue  avgt    3   1310.166 ±   75.076  ms/op
Timer.serialize                                       https://avro.apache.org  avgt    3   2701.126 ±   72.070  ms/op
Timer.serialize                      https://github.com/EsotericSoftware/kryo  avgt    3   4812.091 ±  620.043  ms/op
Timer.serialize                https://developers.google.com/protocol-buffers  avgt    3   1913.270 ±  406.705  ms/op
```

### Benchmarking conclusions

The numbers speak for themselves, but they may be lying.

Size and speed are important considerations, but not the only considerations.

`java.io.Serializable` seems easily worst, but still has some attractive features.

It's less clearcut what is best. The smallest is not the fastest, and
the difference from one choice to another amongst the leaders may not
be worth bothering about.

## Why these won't be the results you get

This is our data model not your data model.

It uses test data for benchmarking, not real data.

## Why you need to be very careful with benchmarking

Obviously you need to measure the right thing, easier to say than do. These are Java benchmarks,
so if the JVM decides to run a GC it will skew performance. More runs should smooth this out.

Java may interfere (eg. https://en.wikipedia.org/wiki/String_interning[String Interning]).
If this is enabled when you benchmark but not enabled in the live system, predictions will
be wrong.

Serialization libraries may use lazy deserialization. Deserialization may provide a link
to the top level object in an object graph, but included objects may not be deserialized
too at that point.

## Bonus optimizations

If you write the serialization logic, you can include application logic optimizations that frameworks won't know about.

For example, on the data model the `issuingCountry` field. The deserialized form may be "_United States_" but you may
choose to serialize it as "_USA_", as this is more compact.

## Summary

There isn't a generic _best_ serialization format that is ideal for all data. There are many
considerations. Some are obvious such as speed and size. Some are less obvious such as licensing
and support.

There is a *most important* criteria though, cross platform inter-operability.
Ignore the Java only options if your application has or will make use of other languages.
Make sure that inter-operable in principle is also in practice, an implementation
is possible for the languages you need.

As indicated by the benchmarks, `HazelcastJsonValue` and `IdentifiedDataSerializable` could be good choices,
but may turn out to be wrong choices for your application.

Be sure to test serialization coding.