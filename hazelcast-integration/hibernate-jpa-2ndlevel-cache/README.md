<h1>Hibernate 2nd Level Cache with Hazelcast by using JPA</h1>
In this repository, you can find a sample implementation of hibernate 2nd level cache with hazelcast by using JPA. You can also find detailed explanation at http://hazelcast.org/ 

<h2>Prerequisites</h2>
You should have installed Apache Maven(http://maven.apache.org/download.cgi).

By default some dependencies added to project in "pom.xml" file as follows:
```
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-hibernate3</artifactId>
    <version>${hazelcast.version}</version>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <version>${hazelcast.version}</version>
</dependency>

<dependency>
    <groupId>javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.12.1.GA</version>
</dependency>

<dependency>
    <groupId>org.apache.derby</groupId>
    <artifactId>derby</artifactId>
    <version>10.10.2.0</version>
</dependency>

<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-entitymanager</artifactId>
    <version>3.6.9.Final</version>
</dependency>

<dependency>
    <groupId>org.hibernate.javax.persistence</groupId>
    <artifactId>hibernate-jpa-2.0-api</artifactId>
    <version>1.0.0.Final</version>
</dependency>
```
But project is also compatible with hibernate 3.X.X versions. You can change these entries accordingly.

<h2>How to Run Sample Application</h2>
1) Compile project using:
```
mvn compile
```
2) Create database using:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CreateDB"
```
3) After running the following code, you can add or delete employees. Start with writing help in the application:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.ManageEmployeeJPA"
```
<h3>Sample Use Case</h3>
Execute the following commands in ManageEmployeeJPA.
```
Command:
add
Id: 1
First Name: Name
Last Name: Surname
Salary: 100
list
Id: 1 
First Name: Name 
Last Name: Surname 
Salary: 100
remove
Key:
1
update
Id: 1
First Name: Name
Last Name: Surname
Salary: 100
Key:
1
```

After application running it creates sample db with 19 records (id range from 1 to 19).
If execute 'show' command with id in range above you will not see any SQL statements in the console (cached entities).

4) Also you may see Hibernate statistics via jconsole.


