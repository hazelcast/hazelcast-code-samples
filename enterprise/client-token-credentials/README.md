## Token-based client authentication example

This example shows how security realms and client security identities can be used to implement authentication based on custom tokens.

The client authentication process uses a custom login module [DowTokenLoginModule.java](src/main/java/com/hazelcast/examples/DowTokenLoginModule.java) which accepts only token-based authentication.

As a valid token is considered the current day-of-week name. Every authenticated client gets a `"monitor"` role assigned. Moreover, if the current day is Monday, then the authenticated client also gets `"admin"` role assigned.

The `"monitor"` role has read access to all `IMap`s and the `"admin"` role has full access to them.

### Login Module

The [DowTokenLoginModule.java](src/main/java/com/hazelcast/examples/DowTokenLoginModule.java)  class extends the Hazelcast's `ClusterLoginModule` abstract class. The method `getName()` is used for  setting a `ClusterIdentityPrincipal` in the JAAS `Subject` and calling `addRole(String)` method adds a `ClusterRolePrincipal` instance to the `Subject`.
