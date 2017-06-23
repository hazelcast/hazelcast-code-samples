#!/bin/bash
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-all/3.8.2/hazelcast-all-3.8.2.jar > hazelcast-all-3.8.2.jar
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-tomcat7-sessionmanager/1.1.1/hazelcast-tomcat7-sessionmanager-1.1.1.jar > hazelcast-tomcat7-sessionmanager-1.1.1.jar
docker-compose up