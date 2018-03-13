package com.javaigua.interconnFlights.actors.messages;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;

import com.javaigua.interconnFlights.domain.Route;
import com.javaigua.interconnFlights.domain.MonthSchedule;

/**
 * A message to signal the result of fetching routes and schedules.
 */
public class RoutesAndSchedules implements Serializable, MessageWithLookUpActorRefName {
  private final GetInterconnections getInterconnections;
  private final Map<String, Route> routes;
  private final Map<String, List<MonthSchedule>> schedules;
  private final ActorRef sender;
  private final ActorRef originalSender;

  public RoutesAndSchedules() {
    this.getInterconnections = new GetInterconnections();
    this.routes = Collections.EMPTY_MAP;
    this.schedules = Collections.EMPTY_MAP;
    this.sender = ActorRef.noSender();
    this.originalSender = ActorRef.noSender();
  }

  public RoutesAndSchedules(GetInterconnections getInterconnections, Map<String, Route> routes,
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
    return getGetInterconnections().getLookUpName() + "_R&S";
  }
}
