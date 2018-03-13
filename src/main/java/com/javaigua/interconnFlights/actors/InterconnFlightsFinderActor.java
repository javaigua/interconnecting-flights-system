package com.javaigua.interconnFlights.actors;

import java.util.Optional;

import scala.concurrent.duration.Duration;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;

import com.javaigua.interconnFlights.actors.messages.*;

/**
 * An actor that performs a distributed retrieval and calculation of interconnecting flights from a target set of routes
 * and schedules.
 *
 * Every GetInterconnections message received by this actor generates a pair of worker child actors that:
 * - fetch routes and flight schedules for all related data in an async non-blocking way.
 * - then calculates a set of the shortest interconnecting flights that are between a target of IATA codes and time range.
 */
public class InterconnFlightsFinderActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  // Supervision strategy for child actors
  private static SupervisorStrategy strategy =
    new OneForOneStrategy(10, Duration.create(1, "minute"),
      DeciderBuilder
        .matchAny(o -> SupervisorStrategy.restart())
        .build());

  /**
   * Convenient actor builder
   */
  public static Props props() {
    return Props.create(InterconnFlightsFinderActor.class);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }

  /**
   * Main entry point of messages handled by this actor
   */
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(GetInterconnections.class, // handle GetInterconnections msgs
        getInterconnections -> {
          FetchRoutesAndSchedule fetchMsg = new FetchRoutesAndSchedule(getInterconnections, getSelf(), getSender());
          getActorRefOrCreate(RoutesAndSchedulesFetcherActor.props(), fetchMsg.getLookUpName())
            .forward(fetchMsg, getContext());
        }
      )
      .match(RoutesAndSchedules.class, // handle RoutesAndSchedules msgs
        routesAndSchedule -> {
          log.debug("status= flights_finder_routes_and_schedule_response, routes= {}, schedules= {}",
            routesAndSchedule.getRoutes(),
            routesAndSchedule.getSchedules());

          CalculateInterconnFlights calculateMsg = new CalculateInterconnFlights(
            routesAndSchedule.getGetInterconnections(),
            routesAndSchedule.getRoutes(),
            routesAndSchedule.getSchedules(),
            getSelf(),
            routesAndSchedule.getOriginalSender());
          getActorRefOrCreate(InterconnFlightsCalculatorActor.props(), calculateMsg.getLookUpName())
            .forward(calculateMsg, getContext());
        }
      )
      .matchAny(unknown -> log.info("{} unknown message received: {}", this.getClass().getName(), unknown))
      .build();
  }

  /**
   * Utility to perform an actor reference lookup by actorName.
   */
  private Optional<ActorRef> getActorRef(String actorName) {
    return getContext().findChild(actorName);
  }

  /**
   * Utility to perform an actor reference lookup by actorName, creating a new InterconnFlightsCalculatorActor
   * in case it is not already present in the actor system.
   */
  private ActorRef getActorRefOrCreate(Props props, String actorName) {
    return getActorRef(actorName).orElseGet(() -> getContext().actorOf(props, actorName));
  }
}
