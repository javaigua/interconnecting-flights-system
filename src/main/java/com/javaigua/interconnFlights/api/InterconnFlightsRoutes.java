package com.javaigua.interconnFlights.api;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import scala.concurrent.duration.Duration;

import akka.util.Timeout;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.PatternsCS;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.domain.*;
import com.javaigua.interconnFlights.actors.messages.*;

/**
 * InterconnFlightsCalculatorActor RESTful API routes mapping.
 */
public class InterconnFlightsRoutes extends AllDirectives {

  final private LoggingAdapter log;
  final private ActorRef interconnFlightsFinderActor;
  final Config config = ConfigFactory.load();
  Timeout timeout = new Timeout(
    Duration.create(config.getInt("application.timeout-millis"), TimeUnit.MILLISECONDS));

  public InterconnFlightsRoutes(ActorSystem system, ActorRef interconnFlightsFinderActor) {
    this.interconnFlightsFinderActor = interconnFlightsFinderActor;
    log = Logging.getLogger(system, this);
  }

  /**
   * Creates routes
   */
  public Route routes() {
    return route(pathPrefix("interconnections", () ->
        route(
          getInterconnections()
        )
    ));
  }

  /**
   * Mapping to handle GET interconnections requests.
   */
  private Route getInterconnections() {
    return pathEnd(() ->
      route(parameter("departure", departureParam ->
        parameter("arrival", arrivalParam ->
          parameter("departureDateTime", departureDateTimeParam ->
            parameter("arrivalDateTime", arrivalDateTimeParam ->
              get(() -> {
                Optional<String> departure = Optional.ofNullable(departureParam);
                Optional<String> arrival = Optional.ofNullable(arrivalParam);
                Optional<LocalDateTime> departureDateTime = parseLocalDateTime(departureDateTimeParam);
                Optional<LocalDateTime> arrivalDateTime = parseLocalDateTime(arrivalDateTimeParam);

                // simple params validation
                if (!departure.isPresent() || !arrival.isPresent() ||
                  !departureDateTime.isPresent() ||  !arrivalDateTime.isPresent() ||
                  departureDateTime.get().isAfter(arrivalDateTime.get())) {
                  return complete(StatusCodes.BAD_REQUEST, "Invalid parameters provided");
                }

                CompletionStage<Set<InterconnFlights>> futureInterconnFlights =
                  PatternsCS.ask(interconnFlightsFinderActor, new GetInterconnections(departure.get(), arrival.get(),
                      departureDateTime.get(), arrivalDateTime.get()),
                    timeout)
                    .thenApply(obj -> (Set<InterconnFlights>) obj);

                return onSuccess(() -> futureInterconnFlights,
                  interconnFlights -> complete(StatusCodes.OK, interconnFlights, Jackson.marshaller()));
                }
              )
            )
          )
        )
      ))
    );
  }

  /**
   * Utility method to parse date time values
   * @param dateTime a date time string with valid ISO format
   * @return an optional value of the date time parsed
   */
  private Optional<LocalDateTime> parseLocalDateTime(String dateTime) {
    try {
      return Optional.of(LocalDateTime.parse(dateTime));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

}
