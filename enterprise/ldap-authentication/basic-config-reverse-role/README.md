# Reverse role mapping with role search recursion (without system account)

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>member</role-mapping-attribute>
  <role-mapping-mode>reverse</role-mapping-mode>
  <role-name-attribute>cn</role-name-attribute>
  <role-recursion-max-depth>5</role-recursion-max-depth>
</ldap>
```

### Valid credentials

```
Username: uid=jduke,ou=Users,dc=hazelcast,dc=com
Password: theduke
```

### Assigned Principals

```
ClusterNamePrincipal: jduke
ClusterRolePrincipal: Admin
ClusterRolePrincipal: Dev
```

*The `ClusterEndpointPrincipal` is not listed.*
