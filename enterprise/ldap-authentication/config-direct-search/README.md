# Direct role mapping with role search recursion (using system account)

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>description</role-mapping-attribute>
  <role-mapping-mode>direct</role-mapping-mode>
  <role-name-attribute>cn</role-name-attribute>
  <role-recursion-max-depth>5</role-recursion-max-depth>
  <system-user-dn>uid=admin,ou=system</system-user-dn>
  <system-user-password>secret</system-user-password>
</ldap>
```

### Valid credentials

```
Username: hazelcast
Password: imdg
```

### Assigned Principals

```
ClusterNamePrincipal: hazelcast
ClusterRolePrincipal: Role1
ClusterRolePrincipal: Role2
ClusterRolePrincipal: Role3
```

*The `ClusterEndpointPrincipal` is not listed.*