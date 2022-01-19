# Hazelcast Dynamic RBAC demo for TLS authentication

This code sample demonstrates a dynamic reconfiguration of role-based access control in Hazelcast.

The project contains three runnable classes:
* `Server`: runs a Hazelcast IMDG server instance embedded inside a simple Java wrapper application;
* `TimestampClient`: puts and gets sample data into an `IMap`;
* `ReplacePermissions`: changes the RBAC configuration dynamically. It gets a new permission configuration XML file path as the argument.

The demo uses native TLS (BoringSSL).

## Requirements

* OpenSSL installed on the system;
* Hazelcast Enterprise license stored in the `~/.hazelcast-code-samples-license` file within your home directory.

## How to run the project

1. Run the `./00-create-keymaterial.sh` script to generate the `keymaterial` folder with certificates and keys;
1. Create a Hazelcast cluster by running one or more instances of `./01-server.sh`;
1. Start a client by running `./02-client.sh` script;
1. *(optional)* You may run another client with full permissions by starting `./02-client.sh resources/admin-hazelcast-client.xml`;
1. Change (grant) client permissions in the cluster by running `./03-grant-permissions.sh` and watch how the client log messages change;
1. Change (revoke) client permissions in the cluster by running `./04-revoke-permissions.sh` and watch how the client log messages change;
