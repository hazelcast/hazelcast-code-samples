<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 

There are two different ways to run this sample application. You can run the sample application with `docker-compose`. You can also manually configure a load balancer connected with two Tomcat or Jetty server to run the application.

<h2>Manual Setup</h2>

<h3>Requirements</h3>
You should have installed Tomcat or Jetty and Apache Maven on your system. There are other requirements already in this repo.

<h3>Load Balancing With Tomcat or Jetty</h3>
To see how application works, you need to start two different servers at different ports. Also you have to connect these servers to a load balancer. You can use apache mod\_jk module for load balancing. Shortly, you have to enable mod\_jk module apache httpd.conf file and specify workers.properties file. You must enter tomcat server ports and configurations to workers.properties file.
You can find detailed explanations at:
</br>
http://tomcat.apache.org/connectors-doc/generic_howto/quick.html

<h3>Build</h3>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/hazelcast-integration/filter-based-session-replication/`
* `mvn install` - Create war file for example

<h3>Tomcat Deployment</h3>
* `cp target/session-replication.war $CATALINA_HOME/webapps/` - Copy war to Tomcat
* Browse to `http://localhost:8080/session-replication/hazelcast

<h3>Jetty Deployment</h3>
* `cp target/session-replication.war $JETTY_HOME/webapps/ - Copy war to Jetty
* Browse to `http://localhost:8080/session-replication/hazelcast`

<h2>How to Run Sample Application in Docker Environment</h2>

You should have installed `Docker` on your system.

1.  Navigate to directory `filter-based-session-replication`  and run `run.sh` shell file from terminal.
`run.sh` creates war file for sample application and build docker images with nginx load balancer and Tomcat servers.  
2. Open a browser and enter `http://localhost:8080/session-replication/hazelcast`

