# Prerequisites

Spring Boot 4 requires JDK 17 or later.

In order to run the code sample, make sure to have PostgresSQL database accessible and configured properly
in `application.properties`.

You can spin-up a PostgreSQL instance easily using Docker:

```shell
docker run --name 2lc-postgres --publish 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword postgres
```

The sample connects to a Hazelcast member as a client. You can start a member in Docker:

```shell
docker run -p 5701:5701 hazelcast/hazelcast:5.5.0
```

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
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-jcache</artifactId>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <version>5.5.0</version>
</dependency>
```

Enable the second-level cache and select Hazelcast's client JCache provider in `application.properties`:

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=jcache
spring.jpa.properties.hibernate.javax.cache.provider=com.hazelcast.client.cache.HazelcastClientCachingProvider
```

For an embedded member, select `com.hazelcast.cache.HazelcastMemberCachingProvider`.
The `hibernate.javax.cache.*` settings are Hibernate's official JCache settings.

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

You can run the sample application using `spring-boot` plugin:

```shell
mvn spring-boot:run
```
