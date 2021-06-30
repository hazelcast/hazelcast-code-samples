# Simple attribute role mapping (using system user)

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>cn</role-mapping-attribute>
  <role-mapping-mode>attribute</role-mapping-mode>
  <user-name-attribute>uid</user-name-attribute>
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
ClusterRolePrincipal: Best IMDG
```