# Simple attribute role mapping (using system account)

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>cn</role-mapping-attribute>
  <role-mapping-mode>attribute</role-mapping-mode>
  <user-name-attribute>uid</user-name-attribute>
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
ClusterRolePrincipal: Best IMDG
```

*The `ClusterEndpointPrincipal` is not listed.*