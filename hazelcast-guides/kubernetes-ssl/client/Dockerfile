FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/client-0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/truststore truststore
ENTRYPOINT ["java","-jar","/app.jar"]