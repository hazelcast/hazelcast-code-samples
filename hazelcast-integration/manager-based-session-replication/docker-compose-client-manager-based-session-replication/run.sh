#!/bin/bash
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-all/3.8.3/hazelcast-all-3.8.3.jar > hazelcast-all-3.8.3.jar
curl https://oss.sonatype.org/content/repositories/releases/com/hazelcast/hazelcast-tomcat85-sessionmanager/1.1.1/hazelcast-tomcat85-sessionmanager-1.1.1.jar > hazelcast-tomcat85-sessionmanager-1.1.1.jar
docker-compose up