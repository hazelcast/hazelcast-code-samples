<h1>ABOUT</h1>
A simple example of Hazelcast JCA connection.
 
<h2>Requirements</h2>
You should have installed JBoss AS or JBoss EAP and Apache Maven on your system. There are other requirements already in this repo.


(`JBoss AS 7+` or `JBoss EAP 6+`), `GlassFish 3.1.2` - Choose free JBoss-AS or choose paid JBoss-EAP<br />
`Apache Maven 3+`<br />
`Hazelcast 3+`<br />

<h1>Build</h1>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/jca-ra`
* `mvn install` - Create war file for example

<h1>JBoss</h1>
`JBoss AS 7+` or `JBoss EAP 6+` - Choose free JBoss-AS or choose paid JBoss-EAP<br />

<h2>JBoss Configuration</h2>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/jca-ra`
* `mvn install` - Create war file for example
* `cp target/lib/hazelcast-jca-rar-3.3-RC3-SNAPSHOT.rar jboss/standalone/deployments`
* `cp target/lib/hazelcast-3.3-RC3-SNAPSHOT.jar jboss/modules/com/hazelcast/main`
* `cp target/lib/hazelcast-jca-3.3-RC3-SNAPSHOT.jar jboss/modules/com/hazelcast/main`
* `cp -R jboss/* $JBOSS_HOME/` - copy all JBoss requirements.

<h2>JBoss Deployment</h2>
* `cp target/hazelcast-jca-example.war $JBOSS_HOME/standalone/deployments/` - Copy war to JBoss
* `$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml` - Run JBoss
* Browse to `http://localhost:8080/hazelcast-jca-example/Hello`

<h1>GlassFish</h1>
<h2>Glassfish Configuration</h2>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/jca-ra`
* `cp -R glassfish/src/* src/` - copy all Glassfish requirements.
* `mvn install` - Create war file for example
* `cp target/lib/hazelcast-jca-rar-3.3-RC3-SNAPSHOT.rar $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/`
* `cp target/lib/hazelcast-3.3-RC3-SNAPSHOT.jar $GLASSFISH_HOME/glassfish/lib`
* `cp target/lib/hazelcast-jca-3.3-RC3-SNAPSHOT.jar $GLASSFISH_HOME/glassfish/lib`

<h2>GlassFish Deployment</h2>
* `cp target/hazelcast-jca-example.war $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/` - Copy war to Glassfish
* `$GLASSFISH_HOME/bin/asadmin start-domain`