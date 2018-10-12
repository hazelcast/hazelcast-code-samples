FROM openjdk:8u171-jre-alpine

RUN apk add --no-cache bash
ADD target/aws-ecs-0.1-SNAPSHOT.jar aws-ecs.jar
CMD ["bash", "-c", "exec java -jar aws-ecs.jar --server.port=0"]