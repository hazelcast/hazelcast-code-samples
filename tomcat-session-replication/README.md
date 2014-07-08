<h2>ABOUT</h2>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 
 
<h3>Tomcat Configuration</h3>

<h4>P2P Deployment</h4>
Follow P2P deployment steps at: http://hazelcast.org/docs/latest/manual/html/sessionreplication.html
<h4>Client-Server Deployment</h4>

Follow Client-Server deployment steps at: http://hazelcast.org/docs/latest/manual/html/sessionreplication.html

<br />

<h2>How to Run Sample Application</h2>


1) Go to `$CATALINA_HOME$/bin` and run startup.sh shell file from terminal.

2) Clone the repository via `git clone https://github.com/bilalyasar/hazelcast-code-samples.git`

3) Then go to tomcat-session-replication folder.

4) Run maven via `mvn package`

5) Maven creates `example.war` file under sessionReplicationApp/target folder.

6) Copy example.war file to `$CATALINA_HOME$/webapps` folder

7) Open a browser and enter `localhost:8080/example/hazelcast`

