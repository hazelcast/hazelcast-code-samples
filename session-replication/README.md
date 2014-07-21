<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 

<h2>Requirements</h2>
You should have installed Tomcat or Jetty and Apache Maven on your system. There are other requirements already in this repo.

<h1>Build</h1>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/session-replication`
* `mvn install` - Create war file for example

<h1>Tomcat Deployment</h1>
* `cp target/session-replication.war $CATALINA_HOME/webapps/` - Copy war to Tomcat
* Browse to `http://localhost:8080/session-replication/hazelcast`

<h1>Jetty Deployment</h1>
* `cp target/session-replication.war $JETTY_HOME/webapps/` - Copy war to Jetty
* Browse to `http://localhost:8080/session-replication/hazelcast`

