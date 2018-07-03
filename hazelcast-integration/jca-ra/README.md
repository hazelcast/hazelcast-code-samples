# ABOUT
A simple example of Hazelcast JCA connection.
 
## Requirements
You should have installed JBoss AS or JBoss EAP and Apache Maven on your system. There are other requirements already in this repo:

* (`JBoss AS 7+` or `JBoss EAP 6+`), `GlassFish 3.1.2` - Choose free JBoss-AS or choose paid JBoss-EAP
* `Apache Maven 3+`
* `Hazelcast 3+`

# Build
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/jca-ra`
* `mvn install` - Create war file for example

# JBoss
`JBoss AS 7+` or `JBoss EAP 6+` - Choose free JBoss-AS or choose paid JBoss-EAP<br />

## JBoss Configuration
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/hazelcast-integration/jca-ra`
* `mvn install` - Create war file for example
* `cp -R target/jboss/* $JBOSS_HOME/` - copy all JBoss requirements.

## JBoss Deployment
* `cp target/hazelcast-jca-example.war $JBOSS_HOME/standalone/deployments/` - Copy war to JBoss
* `$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml` - Run JBoss
* Browse to `http://localhost:8080/hazelcast-jca-example/Hello`

# GlassFish
## Glassfish Configuration
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/hazelcast-integration/jca-ra`
* `cp -R resources/glassfish/src/* src/` - copy all Glassfish requirements.
* `mvn install` - Create war file for example
* `cp -R target/glassfish/* $GLASSFISH_HOME/` - copy all Glassfish requirements.

## GlassFish Deployment
* `cp target/hazelcast-jca-example.war $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/` - Copy war to Glassfish
* `$GLASSFISH_HOME/bin/asadmin start-domain`
