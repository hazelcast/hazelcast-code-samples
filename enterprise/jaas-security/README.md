#hazelcast-security-examples

## Introduction

Sample code for Hazelcast Client Login Security implemented with JAAS.  There are 3 classes.

This example simulates a back-end security authorisation and authentication process for a client
connecting into a Hazelcast cluster and manipulating a map.

### Client

Sets a UserNamePasswordCredentials class on the Client Config and then connects to the Member.

In this class it will create 2 independent client connections to the running Member.  One will connect as an admin user and perform
a PUT operation on an "ImportantMap" and the second client connection will be set-up as a read-onluy user
and try to perform the same PUT operation, this operation will throw an exception.

### Member

This is a Hazelcast Cluster member that is initialised with the _hazelcast.xml_ file

Within the _hazelcast.xml_ we have defined some security properties.

1. We have defined our own LoginModule to be executed when a client first connects.
Called ClientLoginModule
2. We have defined some permissions on the map _importantMap_ for 2 different groups, _readOnlyGroup_ and _adminGroup_.  These groups
are assigned to the client session in the LoginModule.  You'll see that _adminGroup_ has PUT rights on the map whilst _readOnlyGroup_ does not.
```XML
    <security enabled="true">
        <client-login-modules>
            <login-module class-name="com.craftedbytes.hazelcast.security.ClientLoginModule" usage="required">
                <properties>
                    <property name="lookupFilePath">value3</property>
                </properties>
            </login-module>
        </client-login-modules>
        <client-permissions>
            <map-permission name="importantMap" principal="readOnlyGroup">
                <actions>
                    <action>create</action>
                    <action>read</action>
                </actions>
            </map-permission>
            <map-permission name="importantMap" principal="adminGroup">
                <actions>
                    <action>create</action>
                    <action>destroy</action>
                    <action>put</action>
                    <action>read</action>
                </actions>
            </map-permission>
        </client-permissions>
    </security>

    <map name="importantMap"/>
```
Be sure to start this up with the Enterprise key as described in the Requirements section below.

### ClientLoginModule

This is executed on the Member when the Client connects.  This class implements the javax.security.auth.spi.LoginModule

This class is an example of what you should implement yourself to perform authentication operations
against your security back-end of choice (KERBEROS, LDAP, ACTIVE DIRECTORY etc)




## Requirements

The examples requires an Enterprise Hazelcast key as we are using JAAS Security features that are only available in the Enterprise version of Hazelcast.  You can obtain this key via an Enterprise Agreement or
by using a 30 day trial key which can apply for here...

https://hazelcast.com/hazelcast-enterprise-download/

Once you have the key you will need to start the MEMBER Java Process with the following VM switch...

-Dhazelcast.enterprise.license.key=YOUR_ENTERPRISE_KEY_HERE

## Further Reading

It is recommended to read the following guide to Authentication using JAAS...

http://www.javaranch.com/journal/2008/04/authentication-using-JAAS.html

Also please take a look at the Hazelcast Documentation on JAAS Security...

http://www.hazelcast.org/docs/latest/manual/html/nativeclientsecurity.html
