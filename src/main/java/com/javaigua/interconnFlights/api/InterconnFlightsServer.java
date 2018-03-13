package com.javaigua.interconnFlights.api;

import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.actors.InterconnFlightsFinderActor;

/**
 * Main entry point of the Interconnecting Flights API, a RESTful and reactive application.
 *
 * An actor system is created and bound to an http server that handles requests to calculate interconnecting flights
 * by consuming Routes and Schedules APIs.
 */
public class InterconnFlightsServer extends AllDirectives {

  private final InterconnFlightsRoutes interconnFlightsRoutes;

  public InterconnFlightsServer(ActorSystem system, ActorRef interconnFlightsFinderActor) {
    interconnFlightsRoutes = new InterconnFlightsRoutes(system, interconnFlightsFinderActor);
  }

  protected Route createRoute() {
    return interconnFlightsRoutes.routes();
  }

  /**
   * Given the config, an actor system and this routing instance, create an http server and bind them all together.
   */
  private static CompletionStage<ServerBinding> bindHttpServer(Config config, InterconnFlightsServer app,
                                                               ActorSystem system) {
    final Http http = Http.get(system);
    final ActorMaterializer materializer = ActorMaterializer.create(system);
    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
    return http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", config.getInt("application.exposed-port")),
      materializer);
  }

  public static void main(String[] args) throws Exception {
    final Config config = ConfigFactory.load();

    // bootstrap the actor system
    ActorSystem system = ActorSystem.create(config.getString("application.name"), config);
    ActorRef interconnFlightsFinderActor = system.actorOf(InterconnFlightsFinderActor.props(), "interconnFlightsFinder");
    InterconnFlightsServer app = new InterconnFlightsServer(system, interconnFlightsFinderActor);

    // create and bind the http server to the actor system
    final CompletionStage<ServerBinding> binding = bindHttpServer(config, app, system);

    // keep the show running util asked to stop
    System.out.println(String.format("\nServer up at http://localhost:%s/ .... Press Q to shutdown",
      config.getInt("application.exposed-port")));
    char quit;
    do {
      quit = (char) System.in.read();
    } while ('Q' != quit);

    // proper shutdown
    binding
      .thenCompose(ServerBinding::unbind)
      .thenAccept(unbound -> system.terminate());
  }
}
