# Direct role mapping with role search recursion (without system account)

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>description</role-mapping-attribute>
  <role-mapping-mode>direct</role-mapping-mode>
  <role-name-attribute>cn</role-name-attribute>
  <role-recursion-max-depth>5</role-recursion-max-depth>
</ldap>
```

### Valid credentials

```
Username: uid=hazelcast,ou=Users,dc=hazelcast,dc=com
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
