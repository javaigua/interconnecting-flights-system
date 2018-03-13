package com.javaigua.interconnFlights.actors.messages;

import java.io.Serializable;

import akka.actor.ActorRef;

/**
 * A message to signal the command to fetch routes and schedules.
 */
public class FetchRoutesAndSchedule implements Serializable, MessageWithLookUpActorRefName  {
  private final GetInterconnections getInterconnections;
  private final ActorRef sender;
  private final ActorRef originalSender;

  public FetchRoutesAndSchedule() {
    this.getInterconnections = new GetInterconnections();
    this.sender = ActorRef.noSender();
    this.originalSender = ActorRef.noSender();
  }

  public FetchRoutesAndSchedule(GetInterconnections getInterconnections, ActorRef sender, ActorRef originalSender) {
    this.getInterconnections = getInterconnections;
    this.sender = sender;
    this.originalSender = originalSender;
  }

  public GetInterconnections getGetInterconnections() {
    return getInterconnections;
  }

  public ActorRef getSender() {
    return sender;
  }

  public ActorRef getOriginalSender() {
    return originalSender;
  }

  public String getLookUpName() {
    return getGetInterconnections().getLookUpName() + "_FR&S";
  }
}

