# TLS certificate checking login module

This code sample implements a custom JAAS login module: [`sample.ClientCertCheckingLoginModule`](src/main/java/sample/ClientCertChekingLoginModule.java).
It allows additional checks on the client's X.509 certificate during the authentication.
The TLS mutual authentication has to be therefore enabled.

You can configure allow-lists for these certificate properties:
* Subject name (X500) attributes;
* Subject alternative names (SAN extension).

In addition, the Subject name attribute can be used as a role-provider.

## Login module options

| Option name | Default value | Description |
|:-:|:-:|---|
| `checkedAttribute` | `cn` | Attribute in the certificate Subject name which we want compare against values in `allowedAttributeValues`. |
| `allowedAttributeValues` | `""` | Expected/allowed values of the `checkedAttribute`, separated by the `separator`. |
| `allowedSanValues` | `""` | Expected/allowed Subject alternative name (SAN) values, separated by the `separator`. Only DNS and EMAIL types are supported. |
| `roleAttribute` | `""` | Attribute in the certificate Subject name, which will be used as role for the authenticated user. |
| `separator` | `","` | Separator for the allowed values configuration options. |


## Sample configuration

**Member**

```xml
<hazelcast xmlns="http://www.hazelcast.com/schema/config"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.hazelcast.com/schema/config http://www.hazelcast.com/schema/config/hazelcast-config-5.4.xsd">

     <license-key>YOUR_LICENSE_KEY</license-key>
     <network>
          <join>
               <tcp-ip enabled="true">
                    <member-list>
                         <member>127.0.0.1</member>
                    </member-list>
               </tcp-ip>
          </join>
          <ssl enabled="true">
               <properties>
                    <property name="mutualAuthentication">REQUIRED</property>
                    <property name="keyStore">server.p12</property>
                    <property name="trustStorePassword">123456</property>
                    <property name="trustStore">ca.p12</property>
                    <property name="keyStorePassword">123456</property>
               </properties>
          </ssl>
     </network>
     <security enabled="true">
          <realms>
               <realm name="memberRealm">
                    <authentication>
                         <jaas>
                              <login-module class-name="sample.ClientCertCheckingLoginModule" usage="REQUIRED">
                                   <properties>
                                        <property name="allowedSanValues">member1.hazelcast.com,member2.hazelcast.com</property>
                                   </properties>
                              </login-module>
                         </jaas>
                    </authentication>
               </realm>
               <realm name="clientRealm">
                    <authentication>
                         <jaas>
                              <login-module class-name="sample.ClientCertCheckingLoginModule" usage="REQUIRED">
                                   <properties>
                                        <property name="checkedAttribute">cn</property>
                                        <property name="allowedAttributeValues">client22,client23</property>
                                        <property name="roleAttribute">ou</property>
                                   </properties>
                              </login-module>
                         </jaas>
                    </authentication>
               </realm>
          </realms>
          <member-authentication realm="memberRealm"/>
          <client-authentication realm="clientRealm"/>
          <client-permissions>
               <all-permissions principal="admin"/>
          </client-permissions>
     </security>
</hazelcast>
```

**Client**

```xml
<hazelcast-client
    xmlns="http://www.hazelcast.com/schema/client-config"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.hazelcast.com/schema/client-config http://www.hazelcast.com/schema/client-config/hazelcast-client-config-5.4.xsd">

  <network>
    <ssl enabled="true">
      <properties>
        <property name="keyStore">client.p12</property>
        <property name="trustStorePassword">123456</property>
        <property name="trustStore">ca.p12</property>
        <property name="keyStorePassword">123456</property>
      </properties>
    </ssl>
  </network>
</hazelcast-client>
```

With these configurations, the following is required for authentication:

* Members connecting to the cluster have to have a certificate with Subject alternative name (DNS) value either `member1.hazelcast.com` or `member2.hazelcast.com`;
* Clients connecting to the cluster have to have a certificate with Subject attribute `"cn"` equal to `client22` or `client23`;
* If an authenticated client has the Subject name attribute `"ou"`, then its value is used as a role name.

E.g. A Hazelcast client connecting with a trusted certificate having the Subject name `"CN=client22, OU=admin, OU=test, O=Hazelcast Test, C=US"`
will be properly authenticated. The `"cn"` attribute contains one of the allowed values. The client will have assigned two
roles during the authentication `"admin"` and `"test"`. These values are loaded from the `"ou"` attribute in the Subject name.

## Running the example

There are sample test scenarios implemented in the
[`sample.ClientCertCheckingLoginModuleTest`](src/test/java/sample/ClientCertChekingLoginModuleTest.java) JUnit test.

You can use the Maven `test` phase to run it.

```bash
mvn test
```

Hazelcast Enterprise license has to be provided to run this sample. You can place it to a file in your home directory:

```bash
echo "$HAZELCAST_LICENSE" > "${HOME}/.hazelcast-code-samples-license"
```

### Key material for tests

Keystores used for the test are stored in `src` folder. They are PKCS12 keystores with `.p12` suffix and all have password set to `"123456"`.

The keystores were generated by the [`src/create-keymaterial.sh`](src/create-keymaterial.sh) script.

You can use Java `keytool` to list the keystore content:

```bash
keytool -list -keystore src/server.p12 -storepass 123456 -v
```

Output should look like:
```
Keystore type: PKCS12
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: server
Creation date: Aug 25, 2022
Entry type: PrivateKeyEntry
Certificate chain length: 2
Certificate[1]:
Owner: CN=server, O=Hazelcast Test, C=US
Issuer: CN=Test Certification Authority, O=Hazelcast Test, C=US
Serial number: 1
Valid from: Thu Aug 25 16:32:22 CEST 2022 until: Wed Aug 20 16:32:22 CEST 2042
...
```