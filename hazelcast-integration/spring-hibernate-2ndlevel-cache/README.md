# Prerequisites

Spring Boot 3 requires JDK17+

In order to run the code sample, make sure to have PostgresSQL database accessible and configured properly
in `application.properties`.

You can spin-up a PostgreSQL instance easily using Docker:

```shell
docker run --name 2lc-postgres --publish 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword postgres:15
```

If you want to use Hazelcast client, you can start an IMDG instance in a Docker container easily, and then connect to
it:

```shell
docker run -p 5701:5701 hazelcast/hazelcast
```

Remember to double-check if the version is supported by the client.

# Configuration

In order to enable JPA, you need to add a dedicated Spring Boot Starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

In order to configure Hazelcast as second-level cache provider, you need to add two dependencies:

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-hibernate53</artifactId>
    <version>5.2.0</version>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <version>5.5.0</version>
</dependency>
```

And then, we need to configure Hazelcast IMDG local member setting by adding a standard Hazelcast configuration file (`hazelcast.xml` or `hazelcast-client.xml`) file into `src/main/resources`. Spring will recognize the configuration file and autoconfigure either an embedded instance or a native client.

This will also trigger autoconfiguration of the `HazelcastInstance` bean.

The last step involves turning on second-level cache by adding two properties into `application.properties` file:

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=com.hazelcast.hibernate.HazelcastCacheRegionFactory
```

If you want to use Hazelcast client, add `spring.jpa.properties.hibernate.cache.hazelcast.use_native_client=true
`.

And now, once you annotate your entity as `@Cacheable`, it will be cached in a Hazelcast member:

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Book { 
    ...
}
```

If you want to experiment, it's convenient to add custom logic inside the `Runner` class.

# Running

You can the sample application using `spring-boot` plugin:

```shell
mvn spring-boot:run
```
