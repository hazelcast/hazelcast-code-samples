FROM openjdk:8-jre-alpine
COPY hazelcast-embedded-springboot/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]