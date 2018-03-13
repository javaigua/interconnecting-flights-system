package com.javaigua.interconnFlights.actors;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.FiniteDuration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.actors.messages.RoutesAndSchedules;
import com.javaigua.interconnFlights.actors.messages.GetInterconnections;
import com.javaigua.interconnFlights.actors.messages.FetchRoutesAndSchedule;


/**
 * A test suit for the RoutesAndSchedulesFetcherActor class.
 */
public class RoutesAndSchedulesFetcherActorTest extends JUnitSuite {

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
      final Props props = Props.create(RoutesAndSchedulesFetcherActor.class);
      final ActorRef subject = system.actorOf(props);
      final TestKit probe = new TestKit(system);
      final FiniteDuration duration = FiniteDuration.create(config.getInt("application.timeout-millis"),
        TimeUnit.MILLISECONDS);

      GetInterconnections getInterconnections = new GetInterconnections("DUB","WRO", LocalDateTime.now(),
        LocalDateTime.now().plus(2, ChronoUnit.DAYS));

      within(duration("2 seconds"), () -> {
        subject.tell(new FetchRoutesAndSchedule(getInterconnections, getRef(), probe.getRef()), getRef());
        expectMsgPF(duration, "Should receive RoutesAndSchedules", (msg) -> {
          RoutesAndSchedules routesAndSchedules = (RoutesAndSchedules) msg;
          Assert.assertTrue("routesAndSchedules has a command getInterconnections",
            routesAndSchedules.getGetInterconnections().equals(getInterconnections));
          return null;
        });

        expectNoMessage();
        return null;
      });
    }};
  }
}
