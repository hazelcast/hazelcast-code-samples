<h1>Spring Security with Hazelcast</h1>
In this repository, you can find a sample implementation of spring security integration with Hazelcast. You can also find detailed explanation at http://hazelcast.org/


<h2>Prerequisites</h2>
You should have installed Tomcat or Jetty and Apache Maven on your system. There are other requirements already in this repo.

<h2>Build</h2>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/hazelcast-integration/spring-security/`
* `mvn install` - Create war file for example

<h2>Tomcat Deployment</h2>
* `cp target/spring-security.war $CATALINA_HOME/webapps/` - Copy war to Tomcat
* Browse to `http://localhost:8080/spring-security`

<h2>Jetty Deployment</h2>
* `cp target/spring-security.war $JETTY_HOME/webapps/` - Copy war to Jetty
* Browse to `http://localhost:8080/spring-security`

Try to browse `http://localhost:8080/spring-security/admin`  with username: ```user``` and password: ```password```