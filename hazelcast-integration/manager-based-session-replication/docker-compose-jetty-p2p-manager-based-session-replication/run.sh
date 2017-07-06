#!/bin/bash
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-all/3.8.3/hazelcast-all-3.8.3.jar > hazelcast-all-3.8.3.jar
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-jetty9-sessionmanager/1.0.2/hazelcast-jetty9-sessionmanager-1.0.2.jar > hazelcast-jetty9-sessionmanager-1.0.2.jar
curl http://central.maven.org/maven2/org/eclipse/jetty/jetty-nosql/9.3.20.v20170531/jetty-nosql-9.3.20.v20170531.jar > jetty-nosql-9.3.2.v20150730.jar
docker-compose up