# Hibernate 2nd Level Cache with Hazelcast

In this repository, you can find a sample implementation of hibernate 2nd level cache with hazelcast. You can also find detailed explanation at http://hazelcast.org/ 

## Prerequisites

You should have installed Apache Maven(http://maven.apache.org/download.cgi).

It would be great if you have also installed Python 2x(https://www.python.org/downloads/) on your system.

By default `hibernate-core` added to project in `pom.xml` file as follows:

```
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.0.9.Final</version>
</dependency>
```

But project is also compatible with hibernate 3.X.X and 4.X.XQ versions. You can change these entries accordingly.

## How to Run Sample Application

1) Compile project using:

```
mvn compile
```

2) Create database using:

```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CreateTable"
```

3) After running the following code, you can add or delete employees. Start with writing help in the application:

```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.ManageEmployee"
```

### Sample Use Case

Execute the following commands in ManageEmployee. You will see that an employee will be created at the second session but you can see it in the first session too.

```
[1. session]command: list
[1. session]command: change
[2. session]command: add
Id: 1
First Name: Name
Last Name: Surname
Salary: 100
[2. session]command: close
[2. session]command: change
[1. session]command: list
Id: 1 First Name: Name Last Name: Surname Salary: 100
```

## Extras

- You can configure `src/main/resources/hibernate.cfg.xml` file using `src/main/resources/conf.py` script.
