# Hazelcast Dynamic RBAC demo for TLS authentication

Demonstrates dynamic configuration of role-based access control in Hazelcast.

The project contains three runnable classes:
* `Server`: runs a Hazelcast IMDG server instance embedded inside a simple Java wrapper application;
* `TimestampClient`: puts and gets sample data into an `IMap`;
* `ReplacePermissions`: changes the RBAC configuration dynamically. It get's new permission configuration XML file path as the argument.

The demo uses native TLS (BoringSSL).