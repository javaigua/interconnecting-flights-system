package com.javaigua.interconnFlights.actors;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.FiniteDuration;

import org.scalatest.junit.JUnitSuite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.testkit.javadsl.TestKit;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.domain.*;
import com.javaigua.interconnFlights.actors.messages.*;

/**
 * A test suit for the InterconnFlightsFinderActor class.
 */
public class InterconnFlightsFinderActorTest extends JUnitSuite {

  static ActorSystem system;
  static Config config;

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
  public void testHandleGetInterconnections() {
    new TestKit(system) {{
      final Props props = Props.create(InterconnFlightsFinderActor.class);
      final ActorRef subject = system.actorOf(props);
      final FiniteDuration duration = FiniteDuration.create(config.getInt("application.timeout-millis"),
        TimeUnit.MILLISECONDS);

      GetInterconnections getInterconnections = new GetInterconnections("DUB","WRO", LocalDateTime.now(),
        LocalDateTime.now().plus(2, ChronoUnit.DAYS));

      within(duration("2 seconds"), () -> {
        subject.tell(getInterconnections, getRef());
        expectMsgPF(duration, "Should receive Set<InterconnFlights>", (msg) -> {
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
