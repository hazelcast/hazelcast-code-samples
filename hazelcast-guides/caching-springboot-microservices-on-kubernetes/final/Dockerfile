FROM openjdk:8-jdk-alpine

VOLUME /tmp

ARG JAR_FILE=target/hazelcast-spring-app-0.1.0.jar

ADD ${JAR_FILE} hazelcast-spring-demo.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/hazelcast-spring-demo.jar"]
