# Pluggable Variable Replacers

This code sample shows how to implement and use the pluggable configuration replacer feature introduced in Hazelcast
3.10.

## Build and run the samples

Build this sample with Maven.
```
mvn clean install
```

There are several Maven profiles prepared to execute the samples in the `pom.xml`. Use following command to run them:

```bash
mvn exec:java -P [profileName]
```

| Profile name | Related XML file | Description |
| ------------ | ---------------- | ----------- |
| `show.encryption-help` | | Executes the EncryptionReplacer without parameters. It just prints a help. | 
| `generate.encrypted-variable` | `hazelcast-with-replacers.xml` | Executes the EncryptionReplacer with arguments provided. It encrypts the provided String with configuration loadad from the XML file. The resulting Variable is printed to standard output. |
| `run.hazelcast-plain` | `hazelcast-plain.xml` | Starts Hazelcast server without replacers used. |
| `run.hazelcast-with-replacers` | `hazelcast-with-replacers.xml` | Starts Hazelcast server with `EncryptionReplacer` and sample `IdReplacer` used in the configuration. |
| `run.hazelcast-exec-linux` | `hazelcast-exec-linux.xml` | Starts Hazelcast server with sample `ExecReplacer` used in the configuration. The command used (`echo`) should be available on most Unix/Linux-like systems. |
| `run.hazelcast-exec-windows` | `hazelcast-exec-windows.xml` | Starts Hazelcast server with sample `ExecReplacer` used in the configuration. The commands used (`cmd.exe` and its `echo`) should be available on Windows systems. |

## Background

The Variable Replacers are implementations of [ConfigReplacer](https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/java/com/hazelcast/config/replacer/spi/ConfigReplacer.java) interface.
They replaces placeholder strings called Variables in the Hazelcast and Hazelcast client XML configuration files.

The Strings are replaced during loading the configuration.

A Variable to be replaced within the configuration file has the following form:

```
"$" PREFIX "{" VALUE "}"
```

The `PREFIX` is a value returned by `getPrefix()` method and the `VALUE` is a value provided to the 
`getReplacement(String)` method. The result of `ConfigReplacer.getReplacement(String)` method call replaces
the whole Variable string.

## Sample ConfigReplacer implementations

### IdReplacer

|     |     |
| --- | --- |
|**Full class name** | `com.hazelcast.sample.replacer.IdReplacer`|
|**Replacer prefix** | `Id`|

The [IdReplacer](src/main/java/com/hazelcast/sample/replacer/IdReplacer.java) A very simple variable replacer, 
which just returns provided value (i.e. identity function). It's easy to use it as a starting point 
for other custom `ConfigReplacer` implementations.

The `IdReplacer` has no configurable options.

### ExecReplacer

|     |     |
| --- | --- |
|**Full class name** | `com.hazelcast.sample.replacer.ExecReplacer`|
|**Replacer prefix** | `EXEC`|

The [ExecReplacer](src/main/java/com/hazelcast/sample/replacer/ExecReplacer.java) is a ready to use implementation, which
runs external command and uses its standard output as the value for the variable.

#### Options

| Option              | Mandatory | Default value |Description  |
| ---                 | ---       | ---           | ---  |
| `argumentSeparator` | no        |  `"\s+"` *(whitespaces)* | A regular expression used as a separator betwen arguments. It can be used when command path or argument contains whitespaces. |
| `requiresZeroExitCode` | no     |  `true` | A `true`/`false` flag which controls if a non-zero exit code is allowed to continue with the replacement. |

#### Example

```xml
<hazelcast>
    <config-replacers>
        <replacer class-name="com.hazelcast.config.replacer.ExecReplacer">
            <properties>
                <property name="argumentSeparator">#</property>
            </properties>
        </replacer>
    </config-replacers>
    <group>
        <!-- read group name from "hazelcast group.txt" file -->
        <name>$EXEC{cat#/opt/hazelcast group.txt}</name>
    </group>
</hazelcast>
```
