<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 
 
<h3>Tomcat Configuration</h3>

<h4>P2P Deployment</h4>
Follow steps under "Sample P2P Configuration to use Hazelcast Session Replication" title at: https://github.com/hazelcast/hazelcast/blob/master/hazelcast-documentation/src/TomcatSessionReplication.md

<h4>Client-Server Deployment</h4>

Follow steps under "Sample Client/Server Configuration to use Hazelcast Session Replication" title at: https://github.com/hazelcast/hazelcast/blob/master/hazelcast-documentation/src/TomcatSessionReplication.md

<br />

<h2>How to Run Sample Application</h2>


1) Go to `$CATALINA_HOME$/bin` and run startup.sh shell file from terminal.

2) Clone the repository via `git clone https://github.com/hazelcast/hazelcast-code-samples.git`

3) Then go to hazelcast-integration/tomcat-session-replication folder.

4) Run maven via `mvn package`

5) Maven creates `example.war` file under sessionReplicationApp/target folder.

6) Copy example.war file to `$CATALINA_HOME$/webapps` folder

7) Open a browser and enter `localhost:8080/example/hazelcast`

