# Overview

This Docker compose environment can be used to run a Hazelcast 5.3.1 cluster and Management Center. 
It contains the same sample client that is packaged with Viridian, modified for this environment.

# Prerequisites
- Docker Desktop
- maven
- java 11+

# Instructions
Build the Java client
```
mvn clean package
```

Start a 2 node Hazelcast cluster and Management Center
```
docker compose up -d --scale hz=2
```

Run the sample client
```
docker compose run sample-client
```

You can access the Hazelcast Management Center at `localhost:8080`.
Click on "Enable" to enable dev-mode (Management Center does not 
authenticate connections in Dev Mode).




