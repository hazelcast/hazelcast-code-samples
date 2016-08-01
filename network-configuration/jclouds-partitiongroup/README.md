<h1>About</h1>
A simple example of hazelcast `ZONE_AWARE` partition group example with jclouds plugin.

<h2>Requirements</h2>
- You should setup 2 Amazon EC2 instances
- Every instance should have maven and jdk.


<h1>Build</h1>
* `git clone https://github.com/hazelcast/hazelcast-code-samples.git` - Clone the repository
* `cd hazelcast-code-samples/network-configuration/jclouds-partitiongroup`
* `mvn clean install exec:java -Dexec.mainClass="Member"` - Run Main class.

Run main class in two EC2 instances. Then wait for the map filling operation. Map size should be 60 000.
After that, close all jvm processes via `killall -9 java` command in an instance.
Wait for the merge operation, you will see that there is no data loss.

<h1>Configuration</h1>
You should configure `hazelcast.xml` in the folder `resources` accordingly to your EC2 keys.