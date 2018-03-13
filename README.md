# Interconnecting Flights System
A take-home-assignment, by Javier Igua.

To make this exercise compelling the feature of requesting interconnecting flights, of a set of routes between airports and the flight schedule that link them together, is viewed as an optimization problem of calculating the shortest paths of an edge-weighted directed graph.

Given a pair of departing and arrival airports (IATA codes), and a pair of departing and arrival local date times (ISO format), a command message is created and sent to an actor system where this task is broken into multiple different steps executed by actors with specialized roles. 

For instance:
* Handle http message processing and marshall/unmarshall requests/responses in JSON format.
* Fetch and filter routes and flight schedules.
* Create a graph with that information and calculate the shortest paths from source to destination with one or two legs max.
* Orchestrate and supervise the two task explained above.

As a reactive system the following guidelines and desired characteristics are somehow met, or can be easily considered/optimized:
* Responsiveness: To ensure a high-quality user experience, this app aims for high throughput and low latency. 
* Resiliency: Aspects like delegation, isolation, containment and replication (TODO) are at reach with a relative low complexity added to the codebase.
* Elasticity: Different workloads are supported with the same guaranties by scaling out to a much bigger cluster of nodes. Replication can by addressed by different means.
* Message-driven approach: Actors are decoupled by the exchange of immutable messages processed in an asynchronous non-blocking way. Processing is isolated and performed in first-come first-served semantics.


## Implementations details

The main entry point of this application is the  [InterconnFlightsServer.java](src/main/java/com/javaigua/interconnFlights/api/InterconnFlightsServer.java). An actor system is created and bound to an http server that handles requests to calculate interconnecting flights by consuming Routes and Schedules APIs.

Routes for Akka Http processing can be found in [InterconnFlightsRoutes.java](src/main/java/com/javaigua/interconnFlights/api/InterconnFlightsRoutes.java).

An instance of the [InterconnFlightsFinderActor.java](src/main/java/com/javaigua/interconnFlights/actors/InterconnFlightsFinderActor.java) performs a distributed retrieval and calculation of interconnecting flights from a target set of routes and schedules. Every GetInterconnections message received by this actor generates a pair of worker child actors that: 1) fetch routes and flight schedules for all related data in an async non-blocking way. 2) then calculates a set of the shortest interconnecting flights that are between a target of IATA codes and time range.

Every instance of the [RoutesAndSchedulesFetcherActor.java](src/main/java/com/javaigua/interconnFlights/actors/RoutesAndSchedulesFetcherActor.java) fetches routes and flight schedules and filter data by relevance. The Bulkhead pattern is applied to the amount of (http connection pool) resources given to this actor, exposing a back-pressure behaviour and failing fast to clients.

Every instance of the [InterconnFlightsCalculatorActor.java](src/main/java/com/javaigua/interconnFlights/actors/InterconnFlightsCalculatorActor.java) handles the final stage to calculate interconnecting flights with the provided routes and schedule data. For every message received by this actor a directed graph (and associated symbol table) is created and a k-shortest paths calculation is performed. The result is sent to original the actor that requested the operation.

Messages shared between actors can be found in [the messages package](src/main/java/com/javaigua/interconnFlights/actors/messages).

Entities of this application are defined in [the domain package](src/main/java/com/javaigua/interconnFlights/domain).

Text-book implementations of data structures and algorithms for the graph and k-shortest paths are located at [the algorithms package](src/main/java/com/javaigua/interconnFlights/algorithms).

## Execution

### To build a jar with dependencies
First build the jar package:
```
mvn clean package
```

### Run a cluster with a single node
Now run a cluster of a single node:
```
java -jar target/interconnFlights-javaigua-1.0-with-dependencies.jar
```
An example of http requests with the curl command line utility can be found in [curl_commands.txt](src/test/resources/curl_commands.txt).

### Add another node to the cluster

Add a new node to the cluster: 
```
java -Dapplication.exposed-port=8081 -Dclustering.port=2552 -jar target/interconnFlightsColl-javaigua-1.0-with-dependencies.jar
```
TODO: This cluster of two (or more) nodes does not share data yet! This implementation can benefit by adding support for topics like:
 - Akka Distributed Data
 - Akka Sharding
 - Akka Cluster Singleton Manager

### To run with maven
```
mvn compile exec:java
```

### Unit testing 
```
mvn compile test
```