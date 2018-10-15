FROM openjdk:8u171-jre-alpine

RUN apk add --no-cache bash
ADD target/embedded-0.1-SNAPSHOT.jar app.jar
CMD ["bash", "-c", "exec java -jar app.jar"]