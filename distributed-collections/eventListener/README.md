# HazelcastAsEventListener
Using Hazelcast ISet and event listeners for invalidating cache.
This project is to test event listener sequencing in a hazelcast cluster.

Update HazelcastConfig file. Add your local ip address.
I find intelliJ very convienient to start multiple servers on different ports. Start application multiple times by changing the port.

Hit the endpoint "http://localhost:9040/set?dataKeys=123456", and you can see the value getting propogated across multiple nodes.
