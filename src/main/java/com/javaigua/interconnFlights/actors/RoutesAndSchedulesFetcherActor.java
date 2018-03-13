package com.javaigua.interconnFlights.actors;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import scala.concurrent.ExecutionContext;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import static akka.pattern.PatternsCS.pipe;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.javaigua.interconnFlights.domain.*;
import com.javaigua.interconnFlights.actors.messages.*;

/**
 * An actor that fetches routes and flight schedules and filter data by relevance.
 *
 * Bulkhead pattern is applied to the amount of (http connection pool) resources given to this actor,
 * exposing a back-pressure behaviour and failing fast to clients.
 */
public class RoutesAndSchedulesFetcherActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  final Http http = Http.get(context().system());
  final ExecutionContext ec = getContext().dispatcher();
  final Materializer materializer = ActorMaterializer.create(context());

  /**
   * Convenient actor builder
   */
  public static Props props() {
    return Props.create(RoutesAndSchedulesFetcherActor.class);
  }

  /**
   * Main entry point of messages handled by this actor
   */
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(FetchRoutesAndSchedule.class, // handle FetchRoutesAndSchedule msgs
        fetchRoutesAndSchedule -> {
          log.debug("status= routes_and_schedule_starting, desc= {}", fetchRoutesAndSchedule.getLookUpName());
          pipe(fetchRoutesAndSchedule(fetchRoutesAndSchedule), ec).to(getSelf());
        }
      )
      .match(RoutesAndSchedules.class, // handle RoutesAndSchedules msgs
        msg -> msg.getSender().tell(msg, getSelf())
      )
      .matchAny(unknown -> log.info("{} unknown message received: {}", this.getClass().getName(), unknown))
      .build();
  }

  /**
   * Performs the retrieval of the requested routes and schedule data in an async non-blocking manner,
   * even in parallel when possible.
   *
   * @param msg a description of the routes and schedules to be fetched
   * @return a future of the RoutesAndSchedules object that will hold the requested data.
   */
  private CompletableFuture<RoutesAndSchedules> fetchRoutesAndSchedule(final FetchRoutesAndSchedule msg) {
    log.info("status= routes_and_schedules_fetching, departure= {}, arrival= {}, depDateTime= {}, arrDateTime= {}",
      msg.getGetInterconnections().getDeparture(), msg.getGetInterconnections().getArrival(),
      msg.getGetInterconnections().getDepartureDateTime(), msg.getGetInterconnections().getArrivalDateTime());

    Config config = ConfigFactory.load();
    String routesUrlTemplate = config.getString("application.routes-url");
    String schedulesUrlTemplate = config.getString("application.schedules-url");

    return fetchRoutes(routesUrlTemplate)
      .thenApplyAsync(routes -> routes.stream()
          .filter(route -> {
            String departure = msg.getGetInterconnections().getDeparture();
            String arrival = msg.getGetInterconnections().getArrival();
            return departure.equals(route.getAirportFrom()) || arrival.equals(route.getAirportTo());
          })
          .collect(Collectors.toList())
      )
      .thenComposeAsync(routes -> {
        List<CompletableFuture<Map<String, MonthSchedule>>> schedulesFutures = createFetchSchedulesFutures(msg,
          schedulesUrlTemplate, routes);
        log.debug("status= schedules_fetching, schedulesFuturesCount= {}", schedulesFutures.size());

        // execute the schedule futures in parallel
        return CompletableFuture.allOf(schedulesFutures.toArray(new CompletableFuture[0]))
          .thenApplyAsync(v -> schedulesFutures.stream().map(future -> future.join()).collect(Collectors.toList()))
          .thenApplyAsync(schedules -> {
            log.debug("status= schedules_fetched, schedules= {}", schedules);

            // index routes by departure and arrival IATA codes
            final Map<String, Route> routesMap = routes.stream()
              .collect(Collectors.toMap(r -> getKeyFor(r.getAirportFrom(), r.getAirportTo()), r -> r));

            // index schedules by departure and arrival IATA codes
            final Map<String, List<MonthSchedule>> schedulesMap = schedules.stream()
              .map(map -> map.entrySet().stream()
                .filter(e -> e.getValue().getMonth() > 0 && !e.getValue().getDays().isEmpty())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())))
              .flatMap(map -> map.entrySet().stream())
              .collect(Collectors.groupingBy(Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

            log.debug("status= routes_and_schedules_fetched, routes_filtered= {}, schedules_filtered= {}", routes, schedulesMap);
            return new RoutesAndSchedules(msg.getGetInterconnections(), routesMap, schedulesMap, msg.getSender(),
              msg.getOriginalSender());
          });
      })
      .toCompletableFuture();
  }

  /**
   * Fetches all the Routes from the Routes API.
   * @return the collection of available routes.
   */
  private CompletableFuture<List<Route>> fetchRoutes(String routesUrl) {
    log.debug("status= routes_fetching, url= {}", routesUrl);
    return http.singleRequest(HttpRequest.create(routesUrl), materializer)
      .thenComposeAsync(success -> Jackson.unmarshaller(Route[].class).unmarshal(success.entity(), ec, materializer))
      .exceptionally(throwable -> new Route[0])
      .thenApplyAsync(Arrays::asList)
      .thenApplyAsync(routes ->
        routes.stream()
          .filter(route -> route.getConnectingAirport() == null) // filter to only direct routes (no connecting airports)
          .filter(route -> route.getAirportFrom() != null || route.getAirportTo() != null) // filter empty ones
          .collect(Collectors.toList()))
      .toCompletableFuture();
  }

  /**
   * Creates a collection of month schedules futures to be fetched from the Timetable API.
   *
   * @param msg the original message
   * @param routes the fetched routes data
   * @return a collection of month schedules futures to be fetched
   */
  private List<CompletableFuture<Map<String, MonthSchedule>>> createFetchSchedulesFutures(FetchRoutesAndSchedule msg,
                                                                                          String schedulesUrlTemplate,
                                                                                          List<Route> routes) {
    // futures to get all the schedule data from departure to arrival (possibly spans to a month range)
    final int months = getMonthsDifference(msg.getGetInterconnections());
    log.debug("status= routes_fetched_filtered, monthsBetween= {}, routesCount= {}, filtered= {}", months,
      routes.size(), routes);

    List<CompletableFuture<Map<String, MonthSchedule>>> schedulesFutures = new ArrayList<>();
    for (int i = 0; i < routes.size(); i++) {
      String departure = routes.get(i).getAirportFrom();
      String arrival = routes.get(i).getAirportTo();
      final LocalDateTime departureDateTime = msg.getGetInterconnections().getDepartureDateTime()
        .minus(1, ChronoUnit.SECONDS);
      final LocalDateTime arrivalDateTime = msg.getGetInterconnections().getArrivalDateTime()
        .plus(1, ChronoUnit.SECONDS);

      for (int j = 0; j < months; j++) {
        boolean isFirstMonth = j == 0;
        LocalDateTime departureDateTimePlus = departureDateTime.plus(j, ChronoUnit.MONTHS);
        schedulesFutures.add(fetchScheduleForYearMonth(schedulesUrlTemplate, departure, arrival, departureDateTimePlus, arrivalDateTime,
          isFirstMonth));
      }
    }
    return schedulesFutures;
  }

  /**
   * An specific strategy to fetch and filter schedule data for a target departure and arrival IATA codes and temporal
   * time.
   *
   * @param departure the target departure IATA code
   * @param arrival the target arrival IATA code
   * @param departureDateTime the target departure date time
   * @param arrivalDateTime the target arrival date time
   * @param isFirstMonth true if first month in the schedule sequence, false otherwise
   * @return a future taht holds the month schedule for the provided data, index in a map by departure and arrival.
   */
  private CompletableFuture<Map<String, MonthSchedule>> fetchScheduleForYearMonth(String schedulesUrlTemplate,
                                                                                  String departure, String arrival,
                                                                                  LocalDateTime departureDateTime,
                                                                                  LocalDateTime arrivalDateTime,
                                                                                  boolean isFirstMonth) {
    final String scheduleUrl = String.format(schedulesUrlTemplate, departure, arrival, departureDateTime.getYear(),
      departureDateTime.getMonthValue());
    log.debug("status= schedule_fetching, url= {}", scheduleUrl);

    return http.singleRequest(HttpRequest.create(scheduleUrl), materializer)
      .thenComposeAsync(httpResponse ->
        Jackson.unmarshaller(MonthSchedule.class).unmarshal(httpResponse.entity(), ec, materializer))
      .exceptionally(throwable -> new MonthSchedule())
      .thenApplyAsync(monthSchedule -> {
        log.debug("status= month_schedule_unmarshaled, monthSchedule= {}", monthSchedule);

        // filter daySchedule by departure day and its flights by departure day and time
        List<DaySchedule> filteredDays = monthSchedule.getDays().stream()
          .map(daySchedule -> onlyFlightsInBetween(departureDateTime.getYear(), monthSchedule.getMonth(), daySchedule,
            departureDateTime, arrivalDateTime, isFirstMonth))
          .filter(daySchedule -> daySchedule.getFlights() != null && daySchedule.getFlights().size() > 0)
          .collect(Collectors.toList());

        Map<String, MonthSchedule> monthScheduleMap = new HashMap<>();
        monthScheduleMap.put(getKeyFor(departure, arrival), new MonthSchedule(monthSchedule.getMonth(), filteredDays));
        log.debug("status= month_schedule_filtered, filtered= {}, original= {}",
          monthScheduleMap.get(getKeyFor(departure, arrival)), monthSchedule);

        return monthScheduleMap;
      })
      .toCompletableFuture();
  }

  /**
   * Provided a target year, month, a day schedule and a pair of date times (from and to), and a flag indicating the
   * the first month in the parent month schedule, this method filters the flights that are between the
   * resulting range.
   *
   * @param year the target year
   * @param month the target month
   * @param daySchedule a day schedule
   * @param departureDateTime the starting date time
   * @param arrivalDateTime the ending date time
   * @param isFirstMonth true if first month in the parent month schedule, false otherwise
   * @return a filtered day schedule information of the flights in between.
   */
  private DaySchedule onlyFlightsInBetween(Integer year, Integer month,
                                                         DaySchedule daySchedule,
                                                         LocalDateTime departureDateTime, LocalDateTime arrivalDateTime,
                                                         boolean isFirstMonth) {
    log.debug("status= day_schedule_filtering, year_month={}, daySchedule= {}, from_to_time= {}, isFirstMonth= {}",
      year + "_" + month, daySchedule, departureDateTime + " -> " + arrivalDateTime, isFirstMonth);
    List<Flight> filteredFlights = daySchedule.getFlights().stream()
      .filter(flight -> {
        // to filter flights, calculate full date time for departure and arrival
        final LocalDateTime flightDeparture = LocalDateTime.of(year, month, daySchedule.getDay(),
          flight.getDepartureLocalTime().getHour(), flight.getDepartureLocalTime().getMinute(), 0);
        final LocalDateTime flightArrival = LocalDateTime.of(year, month, daySchedule.getDay(),
          flight.getArrivalLocalTime().getHour(), flight.getArrivalLocalTime().getMinute(), 0);

        if (isFirstMonth)
          return departureDateTime.isBefore(flightDeparture) && arrivalDateTime.isAfter(flightArrival);
        else
          return arrivalDateTime.isAfter(flightArrival);
      })
      .collect(Collectors.toList());
    DaySchedule filteredDaySchedule = new DaySchedule(daySchedule.getDay(), filteredFlights);
    log.debug("status= day_schedule_filtered, filtered={}, original= {}", filteredDaySchedule, daySchedule);
    return filteredDaySchedule;
  }

  /**
   * Calculates the difference in months from the departure and arrival date times.
   */
  private static int getMonthsDifference(GetInterconnections getInterconnections) {
    final LocalDateTime departureDateTime = getInterconnections.getDepartureDateTime().minus(1, ChronoUnit.SECONDS);
    final LocalDateTime arrivalDateTime = getInterconnections.getArrivalDateTime().plus(1, ChronoUnit.SECONDS);
    return (int) YearMonth.from(departureDateTime).until(YearMonth.from(arrivalDateTime), ChronoUnit.MONTHS) + 1;
  }

  /**
   * Utility method to generate a key for a given departure and arrival pair values
   */
  private static String getKeyFor(String departureCode, String arrivalCode) {
    return departureCode + "_" + arrivalCode;
  }
}
