# Hazelcast DynaCache on Liberty

Use Java 17 or later and a Liberty runtime with Jakarta Servlet 6.0, Distributed Map, and Jakarta Transactions support.

Build the provider from source at [revision efa3a4a](https://github.com/hazelcast/hazelcast-dynacache/tree/efa3a4ac21807522cff722d7ca056d426b248b2b).

```sh
git clone https://github.com/hazelcast/hazelcast-dynacache.git
cd hazelcast-dynacache
git checkout efa3a4ac21807522cff722d7ca056d426b248b2b
mvn clean verify
```

Install the generated `hazelcast-dynacache/target/hazelcast-dynacache-0.4.esa` as the `usr:hazelcast-dynacache` Liberty feature. Follow the [provider installation instructions](https://github.com/hazelcast/hazelcast-dynacache/blob/efa3a4ac21807522cff722d7ca056d426b248b2b/README.md#installing-hazelcast-dynacache-on-liberty).

Start a Hazelcast 5.7 member in the `dev` cluster on `127.0.0.1:5701`. Set `-Dhazelcast.client.config=/absolute/path/to/hz-client.xml` in the Liberty server's `jvm.options`.

Build the WAR with `mvn -pl hazelcast-integration/dynacache -am package` from the repository root. Use the sample's `server.xml` and set its `appLocation` variable to the absolute path to `target/dynacache-0.1-SNAPSHOT.war`. The servlet is available at `http://localhost:9080/servlet/` when Liberty starts.
