# JSON AuditlogService

This codesample implements a custom `AuditlogService`.
It prints events formatted as JSON Strings to an output file or the standard output stream if no file is configured.

Custom Auditlog implementation has 3 files:
* [JsonAuditlogService](src/main/java/auditlog/JsonAuditlogService.java) class which implements `AuditlogService` interface.
  It contains the main part of the business logic - converting events to JSON and writing them to a file;
* [JsonAuditlogFactory](src/main/java/auditlog/JsonAuditlogFactory.java) class is responsible for creating `JsonAuditlogService` instances.
  It implements the `AuditlogServiceFactory` interface;
* [Event](src/main/java/auditlog/Event.java) class is a simple implementation of `AuditableEvent`.
  The related `Builder` class implementing `EventBuilder` interface is located in the same file.

## Running the example

You can use the exec Maven plugin to run sample applications:

```bash
mvn exec:java -Dexec.mainClass=MainProgrammaticConfig
```

There are 2 main classes in the codesample:
* [MainDeclarativConfig](src/main/java/MainDeclarativConfig.java) (default one) configures the Auditlog declaratively.
  It loads the member configuration from [hazelcast.yml](src/main/resources/hazelcast.yml) file.
  Events are written to the configured output file (`auditlog-json.log`).
* [MainProgrammaticConfig](src/main/java/MainProgrammaticConfig.java) configures the Auditlog programmatically.
  It prints the JSON-formatted event to the `System.out` because no output file is defined.

Hazelcast Enterprise license has to be provided to run this sample.
