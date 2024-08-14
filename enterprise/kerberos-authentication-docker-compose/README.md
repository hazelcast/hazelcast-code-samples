# Hazelcast with Kerberos authentication configured

This Docker compose example shows how Kerberos can be used for member-to-member and client-to-member authentication in Hazelcast.

## Containers in the compose

There are the following containers started in this [Docker compose](docker-compose.yml):

* `kerberos-kdc` - Simple Kerberos server / Key Distribution Center (uses [`data.ldif`](data.ldif) file to populate objects)
* `kerberos-key-material-generator` - helper container used to generate Kerberos KeyTab files
* `member1`, `member2` - Hazelcast members with Kerberos authentication (and identity) configured ([full configuration](hazelcast.xml) used)
* `member3` - Hazelcast member with Kerberos authentication (and identity) configured - using the [simplified configuration](hazelcast-simple-kerberos.xml)
* `hzclient` - Simple Hazelcast client that reads and writes to `timestamps` IMap data structure

## How to run the example

### Build the client application

Use Apache Maven to build the [`timestamp-client`](timestamp-client/src/main/java/TimestampClient.java) application.

```bash
mvn clean install -f timestamp-client/pom.xml
```

### Configure the Hazelcast Enterprise license key

The license key environment variable should be placed in the [`env.properties`](env.properties) file.

```bash
echo "HZ_LICENSEKEY=PutTheProperLicenseKeyHere" > env.properties
```

### Start the Docker compose

To start all the services, run the following command in this directory:

```bash
docker compose up
```

The command will use the [`docker-compose.yml`](docker-compose.yml) file to read service configurations.
