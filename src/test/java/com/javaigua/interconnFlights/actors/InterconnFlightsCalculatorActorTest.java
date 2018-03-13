package com.javaigua.interconnFlights.actors;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.scalatest.junit.JUnitSuite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.domain.InterconnFlights;
import com.javaigua.interconnFlights.actors.messages.GetInterconnections;
import com.javaigua.interconnFlights.actors.messages.CalculateInterconnFlights;

/**
 * A test suit for the InterconnFlightsCalculatorActor class.
 */
public class InterconnFlightsCalculatorActorTest extends JUnitSuite {

  static Config config;
  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    config = ConfigFactory.load("reference");
    system = ActorSystem.create();
  }

  @AfterClass
  public static void teardown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testHandleCalculateInterconnFlights() {
    new TestKit(system) {{
      final Props props = Props.create(InterconnFlightsCalculatorActor.class);
      final ActorRef subject = system.actorOf(props);
      final TestKit probe = new TestKit(system);

      GetInterconnections getInterconnections = new GetInterconnections("DUB","WRO", LocalDateTime.now(),
        LocalDateTime.now().plus(2, ChronoUnit.DAYS));

      within(duration("2 seconds"), () -> {
        subject.tell(new CalculateInterconnFlights(getInterconnections, Collections.EMPTY_MAP, Collections.EMPTY_MAP,
            getRef(), probe.getRef()),
          getRef());
        probe.expectMsgPF(duration("2 seconds"), "Should receive Set<InterconnFlights>", (msg) -> {
          Set<InterconnFlights> interconnFlightsColl = (Set<InterconnFlights>) msg;
          Assert.assertTrue("interconnFlights is a collection", interconnFlightsColl.size() >= 0);
          return null;
        });

        expectNoMessage();
        return null;
      });
    }};
  }
}
