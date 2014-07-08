<h1>ABOUT</h1>
This project is prepared to serve as a sample application for Hazelcast, the leading open source in-memory data grid . Here, Hazelcast's use case is HTTP Session Replication. 
 
<h2>Prerequisites</h2>
You should have installed Apache Tomcat7 and Apache Maven 3.2.1 on your system. Download links are given below. Choose appropriate one for you.

`Apache Tomcat7` - http://tomcat.apache.org/download-70.cgi <br />
`Apache Maven 3.2.1 or higher` - http://maven.apache.org/download.cgi<br />
`Hazelcast EE 3.3 or higher` - http://hazelcast.com/products/hazelcast-enterprise/ 

<h2>Tomcat Configuration</h2>

1) Unzip `hazelcast-EE-<version>.zip` file into the folder `$HAZELCAST_ENTERPRISE_ROOT`. 
Under the `$HAZELCAST_ENTERPRISE_ROOT$/lib` folder, Put `hazelcast-<VERSION>-ee.jar` and `hazelcast-sessions-tomcat7-<VERSION>.jar` to your `$CATALINA_HOME$/lib` folder.<br />

2) Put `hazelcast.xml` file to `$CATALINA_HOME$/lib`. 

3) Open `$HAZELCAST_ENTERPRISE_ROOT/conf/context.xml` file and add this line

`<Manager className="com.hazelcast.session.HazelcastSessionManager"  />`
between `<Context>` and `</Context>` tags.

4) Open `$CATALINA_HOME$/conf/context.xml/conf/server.xml` file and add a listener

`<Listener className="com.hazelcast.session.P2PLifecycleListener"  />`

<br />

<h2>How to Run Sample Application</h2>


1) Go to `$CATALINA_HOME$/bin` and run startup.sh shell file from terminal.

2) Clone the repository via `git clone https://github.com/hazelcastInternsSummer14/sessionReplicationApp.git`

3) Then go to sessionReplicationApp folder.

4) Run maven via `mvn package`

5) Maven creates `example.war` file under sessionReplicationApp/target folder.

6) Copy example.war file to `$CATALINA_HOME$/webapps` folder

7) Open a browser and enter `localhost:8080/example/hazelcast`


<h2>NOTES</h2>
 -You can use Apache Tomcat 6 instead of Tomcat 7.<br />
 -If you want to use client only mode, there must be another hazelcast instance that is working in your network.<br />
 Also you have to update manager tag in `$CATALINA_HOME$/conf/context.xml` file. An example is shown below:
  
   ```xml
 <Context>
      <Manager className="com.hazelcast.session.HazelcastSessionManager"
       clientOnly="true"/>
 </Context>
 ```
 
 -You can find detailed explanations at: http://hazelcast.org/docs/latest/manual/html/sessionreplication.html<br />
 -You can use Hazelcast session replication feature if you have many servers that have load-balancer in front of them. 
 One server goes down, session will not lost.

