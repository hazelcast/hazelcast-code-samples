# Hazelcast Embedded with Eureka discovery

Build all the project with the following command.

```bash
$ mvn clean install
```

## Start Eureka Server

```bash
$ java -jar eureka-server/target/eureka-server-0.1-SNAPSHOT.jar
```

## Start Spring Boot Application with Hazelcast Embedded

Start two Application instances with Hazelcast embedded.

```bash
$ java -jar hazelcast/target/hazelcast-0.1-SNAPSHOT.jar
$ java -jar hazelcast/target/hazelcast-0.1-SNAPSHOT.jar
```

The instances should discover themselves.