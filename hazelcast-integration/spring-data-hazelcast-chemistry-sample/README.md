# Spring Data Hazelcast - "Chemistry" example

![chemistry](http://img.pandawhale.com/87698-I-have-no-idea-what-Im-doing-m-BWx1.jpeg)

## Project Structure

```
.
├── README.md
├── client
│   ├── pom.xml
│   └── src
├── common
│   ├── pom.xml
│   └── src
├── pom.xml
└── server
    ├── pom.xml
    └── src

```

1. `common` project: domain objects (`Element`, `Isotope`), spring configuration, repository definition, service façade.
2. `server` project: Spring Boot application with embedded Hazelcast member + CLI interface to `ChemistryService`
3. `client` project: Spring Boot application, provides REST endpoints to the `ChemistryService`.

## Description
The `spring-data-hazelcast` project includes some basic examples of usage in the unit and integration tests.

The purpose of the *Chemistry* example is to provide a more significant and self-contained application,
that demonstrates the key features and a more realistic deployment, and introduces a slightly more
complex domain model.

This example consists of Hazelcast server modules and client modules. Spring Data Hazelcast functionality
is present in both the server side and client side, so this shows how it can be used with Hazelcast in
embedded mode, in client-server mode, or both.

### Chemistry
The example here uses the elements of the periodic table, as an example of modern chemistry.

#### Element
An element of the periodic table represents something such as Gold, Oxygen, Iron and so on. 

In the periodic table, most fields on the element are unique (name, protons, symbol). We use
the symbol as the key (eg. "au" == gold).

#### Isotope
An isotope is a variation of an element, where the number of passive neutrons may vary.

## Common
The `common` module contains definition needed by servers and any clients you may also run.

Mostly this is standard Spring Data stuff. There are domain classes, repositories, and a service class.

There is very little Hazelcast in this module. 
- The domain classes are tagged with `@KeySpace` to control which Hazelcast _IMap_ is used to store them
- The `CommonConfiguration` class initiates a component scan to find Hazelcast enabled repositories
- The `CommonConfiguration` class takes a Hazelcast bean (from `client` or `server` module) to create the connection between the domain objects/repositories and the underlying store.

### Business Logic
The `ChemistryService` class encapsulates the repositories as a Spring `@Service` bean, and hides that
Elements of the periodic table and their Isotopes are stored separately.

Essentially this service module is the business logic.

## Server
This module creates an executable Jar file embedding the common module, using Spring Shell for command line processing.

Of interest here
- The `ServerCommand` class defines Spring Shell commands and help text
- The `ServerConfiguration` class builds a Hazelcast server from the `hazelcast.xml` file as a Spring bean.

## Client
This module creates an executable Jar file also embedding the common module, using Spring Boot as a web application.

Of interest here
- The `ClientConfiguration` class builds a Hazelcase client from the `hazelcast-client.xml` file as a Spring bean.
- The `ClientController` class defines REST URLs to interrogate the repositories.

## Usage
### Build
From the top-level project, run `mvn install` and this should build the `pom`, `common`, `server` and `client` modules.

As Spring Boot is used, it is necessary to run at least as far as a packaging phase to build the executable jars.

### Start servers
In a command window, start the executable server Jar file using `java -jar server/target/server-0.1-SNAPSHOT.jar`.
You need to run at least one of these, but two is a better number to demonstrate resilience.

#### Server commands
The server starts a Spring Shell with Hazelcast, so you can control it with simple commands.
The main commands of interest are `help`, `load`, `list` and `quit`.

### Start client
In a command window, start the executable client Jar file using `java -jar client/target/client-0.1-SNAPSHOT.jar`.
This starts a client process that should connect to the servers, and respond to web requests.

### URLs
Try `http://localhost:8080`, as this lists the other available URLs.
Experiment with running the `load` and `unload` command on the server to populate and depopulate the test data, to see how the web page responses change. Really though this should be fairly obvious, it's how little coding is required that's the area to focus on.

## Potential improvements
### Spring Shell
The server site uses _spring-shell_ to make an executable Jar file that takes command line input. This should not be too difficult to convert to be a _spring-boot_ `CommandLineRunner` that bootstraps into the command line shell.

### Controller URLs
For simplicity, on the `ClientController` module the landing page returns a list of the other URLs that the controller supports. Adding HATEOAS (_spring-hateoas_), Swagger or `@RepositoryRestController` would be simple steps to make this self-generate.
