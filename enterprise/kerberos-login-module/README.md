# A Kerberos login module for Hazelcast.

Requires a Hazelcast Enterprise licence. Download a trial key from

http://hazelcast.com/hazelcast-enterprise-trial/

## Introduction

Demonstrates how to create a Kerberos Login Module for Hazelcast.

The project builds and produces the `kerberos-login-module.jar` artifact. It relies on the [Java Kerberos Utilities library](https://github.com/jcmturner/java-kerberos-utils) which is available under the Apache License 2.0. The library is included in the project's `lib` directory, and embedded in the produced artifact.

>This module is a PREVIEW release for evaluation purposes only.  
>It is not supported for production use.

## Kerberos

Kerberos assumes that client and servers are running in the `DOMAIN.COM` domain with controller `KDC.DOMAIN.COM`. Then, it relies on:

* A user representing the Hazelcast cluster
* A Service Principal Name for the Hazelcast cluster
* A user running the Hazelcast client

See the *Domain setup* appendix at the end of this document, for details on how to setup the domain.

## Client Configuration

Kerberos authentication is currently supported by the .NET client exclusively. See the [client documentation](https://github.com/hazelcast/hazelcast-csharp-client/blob/master/README.md) section [6.2 Active Directory (Kerberos)](https://github.com/hazelcast/hazelcast-csharp-client/blob/master/README.md#62-active-directory-kerberos) for details.

## Server Configuration

Configuring the server requires:

* The name of the domain (`DOMAIN.COM`) and its controller (`KDC.DOMAIN.COM`)
* The service principal name of the cluster (`hz/cluster1234@DOMAIN.COM`)
* The cluster keytab file (`hzcluster1234.keytab`)

The server must be launched with a few additional options:
```
-Dhazelcast.client.protocol.max.message.bytes=2048
-Djava.security.krb5.conf=/path/to/krb5.conf
-Djava.security.krb5.realm=DOMAIN.COM
-Djava.security.krb5.kdc=KDC.DOMAIN.COM
-Djavax.security.auth.useSubjectCredsOnly=false
-Djava.security.auth.login.config=/path/to/gss-jaas.conf
```

>The `max.message.bytes` is required as it is 1024 by default, and the first message containing the Kerberos token can be about 1600 bytes. This size may have to be adjusted to greater values if the token gets bigger.

In addition, the `kerberos-login-module.jar` file must be added to the `CLASSPATH`. This module is provided as source code in the [Hazelcast Code Samples](https://github.com/hazelcast/hazelcast-code-samples) repository on GitHub in the [kerberos-login-module](https://github.com/hazelcast/hazelcast-code-samples/tree/master/enterprise/kerberos-login-module) directory.

File `gss-jaas.conf` contains:
```
com.sun.security.jgss.accept {
    com.sun.security.auth.module.Krb5LoginModule required
    storeKey=true
    doNotPrompt=true
    debug=true
    useKeyTab=true
    keyTab="/path/to/hzcluster1234.keytab"
    useTicketCache=false
    principal="hz/cluster1234@DOMAIN.COM"
    isInitiator=false
    ;
};
```

File `krb5.conf` contains:
```
[libdefaults]
    default_realm = DOMAIN.COM
    default_tkt_enctypes = aes256-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc
    default_tgs_enctypes = aes256-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc
    permitted_enctypes = aes256-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc
    dns_lookup_kdc = true
    dns_lookup_realm = true
     
[realms]
    DOMAIN.COM = {
        kdc = kdc.domain.com
    }

```

The server configuration contains:
```
<security enabled="true">
  <realms>
    <realm name="client-realm">
      <authentication>
        <jaas>
          <login-module class-name="com.hazelcast.security.KerberosLoginModule" usage="REQUIRED">
            <properties>
              <!-- relax flags check because .NET tokens contain too many things -->
              <property name="relaxFlagsCheck">true</property>
            </properties>
          </login-module>
        </jaas>
      </authentication>
    </realm>
  </realms>
  <client-authentication realm="client-realm"/>
  <client-permissions>
    <!--
        give all permissions to principal hzclient@DOMAIN.COM
    -->
    <all-permissions principal="hzclient@DOMAIN.COM" />
  </client-permissions>
</security>
```

Each Active Directory group for the user is added as a role, identified by the group SID. Therefore, permissions can be assigned to groups in the server configuration.

Finally, the `hzcluster1234.keytab` file must be present in the chosen location.

>The `hzcluster1234.keytab` file is somewhat equivalent to a password, from a security point of view, and must be secured accordingly on the server (it needs to be readable by the Hazelcast server, exclusively).

## Appendix: Domain setup

Create a user account representing the Hazelcast cluster, with the [New-AdUser](https://docs.microsoft.com/en-us/powershell/module/addsadministration/new-aduser) command:

```
PS> new-adUser -name hzcluster1234 -passwordNeverExpires $true -accountPassword (convertTo-secureString "pAssw0rd" -asPlainText -force) -passThru -enabled $true
```

>One might want to create a service account instead, with the [New-AdServiceAccount](<https://docs.microsoft.com/en-us/powershell/module/addsadministration/new-adserviceaccount>) command, but `ktpass` (see below) wants a true user to map the Service Provider Name to. The password here is of no importance, since `ktpass` will reset it.

Create a user account to run the Hazelcast client, with the [New-AdUser](https://docs.microsoft.com/en-us/powershell/module/addsadministration/new-aduser) command:

```
PS> new-adUser -name hzclient -passwordNeverExpires $true -accountPassword (convertTo-secureString "pAssw0rd" -asPlainText -force) -passThru -enabled $true
```

Create one or more user groups to authorize users on the cluster, with the [New-AdGroup](https://docs.microsoft.com/en-us/powershell/module/addsadministration/new-adgroup) command:

```
PS> new-adGroup -name hzcluster1234users -groupScope DomainLocal -passThru
```

>Note the SID (e.g. "S-1-5-21-1680733150-2422849858-4203206891-1129") of the group, you will need it to configure the server. Note that you can always get the SID with the [Get-AdGroup](https://docs.microsoft.com/en-us/powershell/module/addsadministration/get-adgroup) command.

Add the user to the relevant groups, to authorize the user to access the cluster, with the [Add-AdGroupMember](https://docs.microsoft.com/en-us/powershell/module/addsadministration/add-adgroupmember) command:

```
PS> add-adGroupMember -identity hzcluster1234users -members hzclient -passThru
```

Create the Service Principal Name and export the keytab for the server, with the [ktpass](<https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/ktpass>) command:

```
PS> ktpass -princ hz/cluster1234@DOMAIN.COM -mapUser hzcluster1234@DOMAIN.COM -out hzcluster1234.keytab -pass +rndPass -pType KRB5_NT_PRINCIPAL -crypto all   
```

>Note that this assigns a new random password to the service account, thus immediately breaking Kerberos authentication until the new keytab file has been deployed to the server.

Alternatively, it is possible to create a keytab file with only *some* keys but not all of them:

```
PS> ktpass -princ hz/cluster1234@DOMAIN.COM -mapUser hzcluster1234@DOMAIN.COM -out hzcluster1234.keytab -pass * -pType KRB5_NT_PRINCIPAL -crypto AES256-SHA1
PS> ktpass -princ hz/cluster1234@DOMAIN.COM -mapUser hzcluster1234@DOMAIN.COM -out hzcluster1234.keytab -pass * -pType KRB5_NT_PRINCIPAL -crypto AES128-SHA1 -setpass -setupn
```

>Note that it is the required to set the password to an explicit value, which is used in the two invocations. Also note the `-setpass -setupn` parameters, which ensure that the previous keytab is not invalidated. Read for instance [this page](<https://blogs.nologin.es/rickyepoderi/index.php?/archives/104-Two-Tips-about-Kerberos.html>) for more details.

It is possible to list the content of a keytab file using the `ktab` utility, which ships with Java:

```
PS> &"\Program Files\Java\jre1.8.0_241\bin\ktab.exe" -l -e -k .\hzcluster1234.keytab
Keytab name: .\hzcluster1234.keytab
KVNO Principal
---- --------------------------------------------------------------
   6 hz/cluster1234@DOMAIN.COM (17:AES128 CTS mode with HMAC SHA1-96)
   6 hz/cluster1234@DOMAIN.COM (18:AES256 CTS mode with HMAC SHA1-96)

```

## Appendix: KeyTab files rolling deployment

Examine the content of the current keytab file:
```
PS> & 'C:\Program Files\Java\jre1.8.0_241\bin\ktab.exe' -l -e -k .\hzcluster1234.keytab
Keytab name: .\hzcluster1234.keytab
KVNO Principal
---- --------------------------------------------------------------
   9 hz/cluster1234@DOMAIN.COM (1:DES CBC mode with CRC-32)
   9 hz/cluster1234@DOMAIN.COM (3:DES CBC mode with MD5)
   9 hz/cluster1234@DOMAIN.COM (23:RC4 with HMAC)
   9 hz/cluster1234@DOMAIN.COM (18:AES256 CTS mode with HMAC SHA1-96)
   9 hz/cluster1234@DOMAIN.COM (17:AES128 CTS mode with HMAC SHA1-96)
```
Note the current version number (9) and then add keys to the keytab file for the next version number, with the new password (type the password at the prompt):
```
PS> ktpass -princ hz/cluster1234@HZ.LOCAL -mapUser hzcluster1234@DOMAIN.COM -out hzcluster1234.keytab -pass * -pType KRB5_NT_PRINCIPAL -crypto all -setpass -setupn -kvno 10 -in hzcluster1234.keytab

PS> & 'C:\Program Files\Java\jre1.8.0_241\bin\ktab.exe' -l -e -k .\hzcluster1234.keytab
Keytab name: .\hzcluster1234.keytab
KVNO Principal
---- --------------------------------------------------------------
   9 hz/cluster1234@DOMAIN.COM (1:DES CBC mode with CRC-32)
   9 hz/cluster1234@DOMAIN.COM (3:DES CBC mode with MD5)
   9 hz/cluster1234@DOMAIN.COM (23:RC4 with HMAC)
   9 hz/cluster1234@DOMAIN.COM (18:AES256 CTS mode with HMAC SHA1-96)
   9 hz/cluster1234@DOMAIN.COM (17:AES128 CTS mode with HMAC SHA1-96)
  10 hz/cluster1234@DOMAIN.COM (1:DES CBC mode with CRC-32)
  10 hz/cluster1234@DOMAIN.COM (3:DES CBC mode with MD5)
  10 hz/cluster1234@DOMAIN.COM (23:RC4 with HMAC)
  10 hz/cluster1234@DOMAIN.COM (18:AES256 CTS mode with HMAC SHA1-96)
  10 hz/cluster1234@DOMAIN.COM (17:AES128 CTS mode with HMAC SHA1-96)
```
Because of the `-setpass -setupn` options, the version and password are unchanged in AD. Therefore, the AD KDC will continue to issue tokens against version 9. Deploy this keytab file to servers: they will still accept tokens from clients.

Then, update the password of the `hzcluster1234@DOMAIN.COM`, using the same password as given to `ktpass`: *this* updates AD with the new password, and new version. The AD KDC will therefore issue tokens against version 10, and the server will accept these tokens since it already knows about version 10.

This allows for changing the password of the `hzcluster1234@DOMAIN.COM` without interrupting the access to the server.

Changing the password can be achieved with the [Set-ADAccountPassword](https://docs.microsoft.com/en-us/powershell/module/addsadministration/set-adaccountpassword?view=win10-ps) command:

```
PS> Set-ADAccountPassword -Identity hzcluster1234@DOMAIN.COM -Reset -NewPassword (ConvertTo-SecureString -AsPlainText "$newPass" -Force)
```