package com.javaigua.interconnFlights.api;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.FiniteDuration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;

import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.actors.InterconnFlightsFinderActor;

/**
 * A test suit for the RESTful endpoints of this application.
 */
public class InterconnFlightsRoutesTest extends JUnitRouteTest {

  private TestRoute appRoute;

  @Before
  public void initClass() {
    final Config config = ConfigFactory.load("reference");
    ActorSystem system = ActorSystem.create(config.getString("application.name"), config);
    ActorRef interconnFlightsFinderActor = system.actorOf(InterconnFlightsFinderActor.props(), "interconnFlightsFinder");
    InterconnFlightsServer server = new InterconnFlightsServer(system, interconnFlightsFinderActor);
    appRoute = testRoute(server.createRoute());
  }

  @Override
  public FiniteDuration awaitDuration() {
    final Config config = ConfigFactory.load("reference");
    return FiniteDuration.create(config.getInt("application.timeout-millis"), TimeUnit.MILLISECONDS);
  }

  @Test
  public void testHandleInterconnectionsGET() {
    appRoute.run(HttpRequest.GET("/interconnections?departure=DUB&arrival=WRO&" +
      "departureDateTime=2018-03-29T00:00&arrivalDateTime=2018-04-01T23:59"))
      .assertStatusCode(StatusCodes.OK)
      .assertMediaType("application/json");
  }
}
