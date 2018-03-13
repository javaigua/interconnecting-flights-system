package com.javaigua.interconnFlights.actors.messages;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * A message to signal the command to find interconnecting flights.
 */
public class GetInterconnections implements Serializable, MessageWithLookUpActorRefName {
  private final String departure;
  private final String arrival;
  private final LocalDateTime departureDateTime;
  private final LocalDateTime arrivalDateTime;

  public GetInterconnections() {
    this.departure = "";
    this.arrival = "";
    this.departureDateTime = LocalDateTime.now();
    this.arrivalDateTime = LocalDateTime.now();
  }

  public GetInterconnections(String departure, String arrival, LocalDateTime departureDateTime,
                             LocalDateTime arrivalDateTime) {
    this.departure = departure;
    this.arrival = arrival;
    this.departureDateTime = departureDateTime;
    this.arrivalDateTime = arrivalDateTime;
  }

  public String getDeparture() {
    return departure;
  }

  public String getArrival() {
    return arrival;
  }

  public LocalDateTime getDepartureDateTime() {
    return departureDateTime;
  }

  public LocalDateTime getArrivalDateTime() {
    return arrivalDateTime;
  }

  public Integer getDepartureYear() {
    return departureDateTime.getYear();
  }

  public Integer getDepartureMonth() {
    return departureDateTime.getMonthValue();
  }

  public Integer getDepartureDay() {
    return departureDateTime.getDayOfMonth();
  }

  public LocalTime getDepartureHour() {
    return LocalTime.from(departureDateTime);
  }

  public Integer getArrivalYear() {
    return arrivalDateTime.getYear();
  }

  public Integer getArrivalMonth() {
    return arrivalDateTime.getMonthValue();
  }

  public Integer getArrivalDay() {
    return arrivalDateTime.getDayOfMonth();
  }

  public LocalTime getArrivalHour() {
    return LocalTime.from(arrivalDateTime);
  }

  public LocalDateTime getDepartureDateTimeForFilter() {
    return LocalDateTime.of(
      getDepartureYear(),
      getDepartureMonth(),
      getDepartureDay(),
      getDepartureHour().getHour(),
      getDepartureHour().getMinute())
      .minus(1, ChronoUnit.SECONDS);
  }

  public LocalDateTime getArrivalDateTimeForFilter() {
    return LocalDateTime.of(
      getArrivalYear(),
      getArrivalMonth(),
      getArrivalDay(),
      getArrivalHour().getHour(),
      getArrivalHour().getMinute())
      .plus(1, ChronoUnit.SECONDS);
  }

  public String getLookUpName() {
    return new StringBuffer().append(departure).append("_").append(arrival).append("_")
      .append(getDepartureYear()).append("_").append(getDepartureMonth()).append("_")
      .append(getDepartureDay()).append("_").append(getDepartureHour()).append("_")
      .append(getArrivalYear()).append("_").append(getArrivalMonth()).append("_")
      .append(getArrivalDay()).append("_").append(getArrivalHour())
      .toString();
  }
}
