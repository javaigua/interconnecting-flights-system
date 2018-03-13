package com.javaigua.interconnFlights.actors;

import java.util.*;
import java.util.stream.Collectors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.javaigua.interconnFlights.domain.*;
import com.javaigua.interconnFlights.algorithms.*;
import com.javaigua.interconnFlights.actors.messages.*;

/**
 * An actor that handles the final stage to calculate interconnecting flights with the provided routes and schedule data.
 *
 * For every message received by this actor a directed graph (and associated symbol table) is created and
 * a k-shortest paths calculation is performed. The result is sent to original the actor that requested the operation.
 */
public class InterconnFlightsCalculatorActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  /**
   * Convenient actor builder
   */
  public static Props props() {
    return Props.create(InterconnFlightsCalculatorActor.class);
  }

  /**
   * Main entry point of messages handled by this actor
   */
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(CalculateInterconnFlights.class,
        calculateInterconnFlights -> {
          ActorRef actorRef = calculateInterconnFlights.getOriginalSender();
          actorRef.tell(calculateInterconnectingFlights(calculateInterconnFlights), getSelf());
        }
      )
      .matchAny(unknown -> log.info("{} unknown message received: {}", this.getClass().getName(), unknown))
      .build();
  }

  /**
   * Handles CalculateInterconnFlights messages sent to this actor.
   *
   * Creates a directed graph (and associated symbol table) and performs k-shortest paths calculation
   * with the given routes and schedules information.
   *
   * @param msg a CalculateInterconnFlights message to be processed
   * @return a InterconnFlightsCollection object with interconnecting flights with 1 or two 2 legs
   */
  private Set<InterconnFlights> calculateInterconnectingFlights(CalculateInterconnFlights msg) {
    final Map<String, Route> routes = msg.getRoutes();
    final Map<String, List<MonthSchedule>> schedules = msg.getSchedules();
    final String source = msg.getGetInterconnections().getDeparture();
    final String destination = msg.getGetInterconnections().getArrival();

    log.info("status= flights_calculator_starting, routes_size= {}, schedule_size= {} ", routes.size(), schedules.size());

    // Create SymbolDigraph with routes and schedule
    SymbolDigraph symbolDigraph = new SymbolDigraph(routes, schedules);
    EdgeWeightedDigraph graph = symbolDigraph.digraph();
    log.debug("status= flights_calculator_graph_created, symbolDigraph= {}", symbolDigraph.toString());

    Set<InterconnFlights> interconnFlights = new LinkedHashSet<>();
    if (!msg.getRoutes().isEmpty() && !msg.getSchedules().isEmpty() &&
      symbolDigraph.contains(source) && symbolDigraph.contains(destination)) {
      int origIndex = symbolDigraph.indexOf(source);
      int destIndex = symbolDigraph.indexOf(destination);

      List<KShortestPaths.Path> kShortestPaths = KShortestPaths.getKShortestPaths(graph, origIndex, destIndex, 2);
      log.info("status= flights_calculator_kshortest_paths_calculated, orig_dest= {}, paths= {} ",
        origIndex + "_" + destIndex, printShortestPaths(symbolDigraph, kShortestPaths));

      for (KShortestPaths.Path path : kShortestPaths) {
        Set<InterconnFlight> interconnFlightsArray = new LinkedHashSet<>();
        for (DirectedEdge e : path.getPath()) {
          interconnFlightsArray.add(
            new InterconnFlight(e.flight().getNumber(), symbolDigraph.nameOf(e.from()),
              symbolDigraph.nameOf(e.to()), e.flight().getDepartureTime(), e.flight().getArrivalTime()));
        }
        interconnFlights.add(
          new InterconnFlights(
            interconnFlightsArray.stream().map(f -> f.getNumber()).collect(Collectors.joining("_")),
            interconnFlightsArray.toArray(new InterconnFlight[]{})));
      }
    }
    return interconnFlights;
  }

  /**
   * Concatenates the given digraph data for logging purposes
   */
  private String printShortestPaths(SymbolDigraph symbolDigraph, List<KShortestPaths.Path> kShortestPaths) {
    StringBuilder sbPath = new StringBuilder();
    for (KShortestPaths.Path path : kShortestPaths) {
      for (DirectedEdge e : path.getPath()) {
        sbPath.append(symbolDigraph.nameOf(e.from())).append("->").append(symbolDigraph.nameOf(e.to()))
          .append("(").append(e.flight()).append(") ");
      }
      sbPath.append(" | ");
    }
    return sbPath.toString();
  }
}
