<h1>ABOUT</h1>
A simple example of Hazelcast JCA connection.
 
<h2>Requirements</h2>
You should have installed JBoss AS or JBoss EAP and Apache Maven on your system. There are other requirements already in this repo.


(`JBoss AS 7+` or `JBoss EAP 6+`), `GlassFish 3.1.2` - Choose free JBoss-AS or choose paid JBoss-EAP<br />
`Apache Maven 3+`<br />
`Hazelcast 3+`<br />

<h2>Build Example</h2>
* `git clone https://github.com/hazelcastInternsSummer14/hazelcast-jca-example.git` - Clone repo into the local
* `cd hazelcast-jca-example`
* `mvn install war:war` - Create war file for example

<h2>JBoss Configuration</h2>
`JBoss AS 7+` or `JBoss EAP 6+` - Choose free JBoss-AS or choose paid JBoss-EAP<br />

* `git clone https://github.com/hazelcastInternsSummer14/hazelcast-jca-example.git` - Clone repo into the local
* `cd hazelcast-jca-example`
* `mvn install war:war` - Create war file for example
* `cp -R jboss/* $JBOSS_HOME/` - copy all JBoss requirements.
* `cp target hazelcast-jca-example.war $JBOSS_HOME/standalone/deployments/` - Copy war to JBoss
* `$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml` - Run JBoss
* Browse to `http://localhost:8080/hazelcast-jca-example/Hello`

<h2>Glassfish Configuration</h2>
* `git clone https://github.com/hazelcastInternsSummer14/hazelcast-jca-example.git` - Clone repo into the local
* `cd hazelcast-jca-example`
* `cp -R glassfish/src/* $GLASSFISH_HOME/src/` - copy all Glassfish requirements.
* `cp -R glassfish/server/* $GLASSFISH_HOME/` - copy all Glassfish requirements.
* `mvn install war:war` - Create war file for example
* `cp target hazelcast-jca-example.war $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/` - Copy war to Glassfish
* `$GLASSFISH_HOME/bin/asadmin start-domain`