<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 

 
<h3>Tomcat Configuration</h3>

Use Tomcat 10.1 with the Jakarta-compatible `hazelcast-tomcat10-sessionmanager` module. If a matching release is unavailable, run `mvn -pl tomcat10 -am package -DskipTests` from the [session manager repository](https://github.com/hazelcast/hazelcast-tomcat-sessionmanager).

<h4>P2P Deployment</h4>
Follow steps under "Deploying P2P for Tomcat" title at: 
https://github.com/hazelcast/hazelcast-tomcat-sessionmanager#deploying-p2p-for-tomcat

<h4>Client-Server Deployment</h4>

Follow steps under "Deploying Client-Server for Tomcat" title at: 
https://github.com/hazelcast/hazelcast-tomcat-sessionmanager#deploying-client-server-for-tomcat


<h3>Load Balancing With Tomcat</h3>
To see how application works, you need to start two different tomcats at different ports. Also you have to connect these tomcats to a load balancer. You can use apache mod\_jk module for load balancing. Shortly, you have to enable mod\_jk module apache httpd.conf file and specify workers.properties file. You must enter tomcat server ports and configurations to workers.properties file.
You can find detailed explanations at:
</br>
https://tomcat.apache.org/connectors-doc/common_howto/quick.html

<h3>Jetty Configuration</h3>

For Jetty 12.1, enable `ee10-deploy` and `ee10-jsp`, then deploy `example.war` to `$JETTY_BASE/webapps`.
Use `session-store-hazelcast-embedded` for P2P deployment or `session-store-hazelcast-remote` to connect to an existing cluster.
See [Jetty's Hazelcast session configuration](https://jetty.org/docs/jetty/12.1/operations-guide/session/index.html#_modules_for_persistent_http_sessions_hazelcast).

<h2>How to Run the Sample on Tomcat</h2>


1) Go to `$CATALINA_HOME$/bin` and run startup.sh shell file from terminal.

2) Clone the repository via `git clone https://github.com/hazelcast/hazelcast-code-samples.git`

3) Then go to `hazelcast-integration/manager-based-session-replication` folder.

4) Run maven via `mvn package`

5) Maven creates `example.war` file under the current module’s `target/` directory.

6) Copy example.war file to `$CATALINA_HOME$/webapps` folder

7) Open a browser and enter `localhost:8080/example/hazelcast`

