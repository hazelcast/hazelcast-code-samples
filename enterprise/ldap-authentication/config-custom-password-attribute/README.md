# User filter and Custom password attribute

This scenario is only possible with using the system account.

### LDAP authentication options

```xml
<ldap>
  <url>ldap://127.0.0.1:10389</url>
  <role-mapping-attribute>uid</role-mapping-attribute>
  <role-mapping-mode>attribute</role-mapping-mode>
  <user-name-attribute>cn</user-name-attribute>
  <password-attribute>sn</password-attribute>
  <user-context>ou=Users,dc=hazelcast,dc=com</user-context>
  <user-filter>(&amp;(cn={login})(objectClass=inetOrgPerson))</user-filter>
  <system-user-dn>uid=admin,ou=system</system-user-dn>
  <system-user-password>secret</system-user-password>
</ldap>
```

### Valid credentials

```
Username: Best IMDG
Password: Hazelcast
```

### Assigned Principals

```
ClusterNamePrincipal: Best IMDG
ClusterRolePrincipal: hazelcast
```

*The `ClusterEndpointPrincipal` is not listed.*