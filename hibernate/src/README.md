<h1>Hibernate 2nd Level Cache with Hazelcast</h1>
In this repository, you can find a sample implementation of hibernate 2nd level cache with hazelcast. You can also find detailed explanation at http://hazelcast.org/ 

<h2>Prerequisites</h2>
You should have installed Apache Maven(http://maven.apache.org/download.cgi).

It would be great if you have also installed Python 2x(https://www.python.org/downloads/) on your system.

By default "hibernate-core" and "hazelcast-hibernate4" added to project as follows in "pom.xml" file
```
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>4.3.5.Final</version>
</dependency>


<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-hibernate4</artifactId>
    <version>3.2.3</version>
</dependency>
```
But project is also compatible with hibernate 3.X.X versions. You can change these entries accordingly.

<h2>How to Run Sample Application</h2>
1) clone the repository to your local using:
```
git clone https://github.com/hazelcastInternsSummer14/hibernate.git
```
2) go to "hibernate" folder

3) Compile project using:
```
mvn compile
```
4) Create database using:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CreateDB"
```
5) After running the following code, you can add or delete employees. Start with writing help in the application:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.ManageEmployee"
```
<h3>Sample Use Case</h3>
Execute the following commands in ManageEmployee. You will see that an employee will be created at the second session but you can see it in the first session too.
```
[1. session]command: list
[1. session]command: change
[2. session]command: add
Id: 1
First Name: Ali
Last Name: Veli
Salary: 100
[2. session]command: close
[2. session]command: change
[1. session]command: list
Id: 1 First Name: Ali Last Name: Veli Salary: 100
```
<h2>Extras</h2>
- You can configure "src/main/resources/hibernate.cfg.xml" file using "src/main/resources/conf.py" script.
