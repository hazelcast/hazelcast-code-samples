# Simple authentication sample

This code sample shows simple authentication configuration support in Hazelcast.

The **simple authentication** means users and their roles are defined directly within the Hazelcast member configuration.

**Example:**

```xml
<realm name="simpleRealm">
    <authentication>
        <simple>
            <user username="test" password="a1234">
                <role>monitor</role>
                <role>hazelcast</role>
            </user>
            <user username="root" password="secret">
                <role>admin</role>
            </user>
        </simple>
    </authentication>
</realm>
```

The example has 3 components:
* Hazelcast member ([Member.java](src/main/java/Member.java)) - taking declarative config from the [hazelcast.xml](hazelcast.xml)
* Programatically configured Hazelcast member ([ProgrammaticMember.java](src/main/java/ProgrammaticMember.java))
* Hazelcast client ([TimestampClient.java](src/main/java/TimestampClient.java)) - taking declarative config from the [hazelcast-client.xml](hazelcast-client.xml)


## Building the project

```
mvn clean install
```

## Running the example

Hazelcast Enterprise license has to be provided to run this sample.

```
# configure the license first
export HZ_LICENSEKEY=YOUR_LICENSE_KEY_HERE

# start Hazelcast member
mvn exec:java -Dexec.mainClass=Member

# you can also start the programatically configured Hazelcast member
mvn exec:java -Dexec.mainClass=ProgrammaticMember \
  -Dhazelcast.enterprise.license.key=$HZ_LICENSEKEY

# start Hazelcast client
mvn exec:java -Dexec.mainClass=TimestampClient

# start unauthorized Hazelcast client
mvn exec:java -Dexec.mainClass=TimestampClient \
  -Dhazelcast.client.config=hazelcast-client-unauthorized.xml
```
