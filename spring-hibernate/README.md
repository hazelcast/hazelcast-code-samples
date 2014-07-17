<h1>Spring and Hibernate with Hazelcast</h1>
In this repository, you can find a sample implementation of spring and hibernate with hazelcast maploaders. You can also find detailed explanation at http://hazelcast.org/
<h2>Prerequisites</h2>
- You should have installed Apache Maven(http://maven.apache.org/download.cgi).
<h2>How to Run Sample Application</h2>
- Compile the project using:
```
mvn compile
```
- Run the project using:
```
mvn exec:java -Dexec.mainClass="com.hazelcast.springHibernate.Application"
```