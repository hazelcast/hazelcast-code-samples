Hazelcast Spring Configuration
==============================

This application is intended to serve as an example of configuring Hazelcast project to use Spring Framework. Detailed information can be found at: http://hazelcast.org/docs/3.2/manual/html/springintegration.html#spring-integration/

Prerequisites
-------------

- Apache Maven ( http://maven.apache.org/download.cgi )

Spring Framework dependencies can be added using the file: "pom.xml".


Running Sample Application
--------------------------

1) Clone the repository to your local using:

    git clone git@github.com:hazelcast/hazelcast-code-samples.git

2) Go to "spring-configuration" folder

3) Compile the project with:

    mvn compile
    
4) Run the following commands respectively:

    mvn exec:java -Dexec.mainClass="com.hazelcast.springconfiguration.HazelcastDataTypes"
    
    mvn exec:java -Dexec.mainClass="com.hazelcast.springconfiguration.SpringClient"
