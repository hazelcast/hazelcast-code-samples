<h1>ABOUT</h1>
A simple example of hazelcast-jclouds module.

<h2>Requirements</h2>
You should include dependencies for providers served under jclouds lab. This example only works for google-compute engine and providers,
 served under jclouds-all-compute. See list of providers here : https://jclouds.apache.org/reference/providers/#compute

<h1>Build</h1>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone repo into the local
* `cd hazelcast-code-samples/network-configuration/jclouds`
* `mvn install` - Create an uber jar with jclouds dependencies, it is jclouds-0.1-SNAPSHOT-jar-with-dependencies.jar under target folder.

<h1>Configuration</h1>
You should configure `hazelcast.xml` under `resources` according to your cloud provider.
You can find latest configuration options here : http://docs.hazelcast.org/docs/latest-dev/manual/html-single/index.html#discovering-members-with-jclouds

<h1>Deployment</h1>
Deploy `jclouds-0.1-SNAPSHOT-jar-with-dependencies.jar` to your cloud machines <br />
Run `java -cp jclouds-0.1-SNAPSHOT-jar-with-dependencies.jar StartServer` on machines you deployed<br />
