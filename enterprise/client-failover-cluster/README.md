# Client Fail-over/Blue-Green Test

This code sample shows how to configure clients with multiple cluster configs for fail-over or blue-green scenarios.
start.sh simulates a failing cluster. Client can be used with same configuration for blue-green scenarios also. 
In this case ,first cluster does not fail but clients are blacklisted from the first cluster on command. 
Blacklisting is done via Management Center UI.

## Build and run the samples

Build this sample with Maven. and run start.sh
```
mvn clean install
sh start.sh
```

For the configuration and expected behaviour see start.sh

