## User Code Namespaces API configuration code examples. 

Requires a Hazelcast Enterprise licence.  Download a trial key from https://hazelcast.com/get-started/


### 1. Statically configuring a namespace with a JAR containing a single class. 

Run Member:

```
cd user-code-namespaces-member
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespace.staticconfig.jar.Member
```

**Member code walkthrough:**

- Enables user code namespaces feature on Config.
- Creates a new `UserCodeNamespaceConfig` with name `ucn1` and adds the external JAR URL.
- Adds the `UserCodeNamespaceConfig` to the `Config`.
- Creates a `MapConfig` referencing the namespace `ucn1`.
- Starts the member with config.
- Puts K,V (key, 0) into the map. 

Run Client:

```
cd user-code-namespaces-client
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespaces.staticconfig.Client
```

Observe the value for key is incremented by 1 each time the client executes.

_____

### 2. Statically configuring a namespace with a single class.

Run Member:

```
cd user-code-namespaces-member
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespace.staticconfig.classconfig.Member
```

**Member code walkthrough:**

- Loads the class `IncrementingEntryProcessor` from outside the classpath.
- Enables user code namespaces feature on config.
- Creates a new `UserCodeNamespaceConfig` and adds the loaded `IncrementingEntryProcessor` class to it.
- Adds the `UserCodeNamespaceConfig` to the `Config`.
- Creates a `MapConfig` referencing the namespace.
- Starts the member.

Run Client:

```
cd user-code-namespaces-client
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespaces.staticconfig.Client
```

Observe the value for key is incremented by 1 each time the client executes.

-----

### 3. Dynamically configured namespace with a jar containing a single class. 


Run Member:

```
cd user-code-namespaces-member
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespace.dynamic.Member
```

**Member code walkthrough:**

- Enables user code namespaces feature on config.
- Creates a new `UserCodeNamespaceConfig` named `ucn1` with no resources. 
*Note - this config must exist on the running instance before the client can add to it.*
- Creates a `MapConfig` referencing the namespace.
- Puts K,V (key, 0) into the map.
- Starts the member.


Run Client:

```
cd user-code-namespaces-client
mvn clean compile exec:java -Dexec.mainClass=com.hazelcast.namespaces.dyamicconfig.Client
```

**Client Code walkthrough:**

- Starts the client.
- Creates a new `UserCodeNamespaceConfig` with `ucn1` as the name.
- Adds the `IncrementingEntryProcessor` class from the classpath to the `UserCodeNamespaceConfig`.
- Gets the hazelcast config from the client instance and adds the `UserCodeNamespaceConfig` to the `Config`.


Observe the value for key is incremented by 1 each time the client executes.
