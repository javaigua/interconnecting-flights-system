package com.javaigua.interconnFlights.actors.messages;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;

import com.javaigua.interconnFlights.domain.MonthSchedule;
import com.javaigua.interconnFlights.domain.Route;


/**
 * A message to signal the command to calculate interconnecting flights.
 */
public class CalculateInterconnFlights  implements Serializable, MessageWithLookUpActorRefName {
  private final GetInterconnections getInterconnections;
  private final Map<String, Route> routes;
  private final Map<String, List<MonthSchedule>> schedules;
  private final ActorRef sender;
  private final ActorRef originalSender;

  public CalculateInterconnFlights() {
    this.getInterconnections = new GetInterconnections();
    this.routes = Collections.EMPTY_MAP;
    this.schedules = Collections.EMPTY_MAP;
    this.sender = ActorRef.noSender();
    this.originalSender = ActorRef.noSender();
  }

  public CalculateInterconnFlights(GetInterconnections getInterconnections, Map<String, Route> routes,
                                   Map<String, List<MonthSchedule>> schedules, ActorRef sender, ActorRef originalSender) {
    this.getInterconnections = getInterconnections;
    this.routes = routes;
    this.schedules = schedules;
    this.sender = sender;
    this.originalSender = originalSender;
  }

  public GetInterconnections getGetInterconnections() {
    return getInterconnections;
  }

  public Map<String, Route> getRoutes() {
    return routes;
  }

  public Map<String, List<MonthSchedule>> getSchedules() {
    return schedules;
  }

  public ActorRef getSender() {
    return sender;
  }

  public ActorRef getOriginalSender() {
    return originalSender;
  }

  public String getLookUpName() {
    return getInterconnections.getLookUpName() + "_CIF";
  }
}