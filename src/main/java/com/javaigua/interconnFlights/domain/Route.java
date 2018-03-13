package com.javaigua.interconnFlights.domain;

/**
 * A representation of the information of a Route.
 */
public class Route {
  private final String airportFrom;
  private final String airportTo;
  private final String connectingAirport;
  private final Boolean newRoute;
  private final Boolean seasonalRoute;
  private final String operator;
  private final String group;

  public Route() {
    this.airportFrom = "";
    this.airportTo = "";
    this.connectingAirport = "";
    this.newRoute = false;
    this.seasonalRoute = false;
    this.operator = "";
    this.group = "";
  }

  public Route(String airportFrom, String airportTo, String connectingAirport, Boolean newRoute,
               Boolean seasonalRoute, String operator, String group) {
    this.airportFrom = airportFrom;
    this.airportTo = airportTo;
    this.connectingAirport = connectingAirport;
    this.newRoute = newRoute;
    this.seasonalRoute = seasonalRoute;
    this.operator = operator;
    this.group = group;
  }

  public String getAirportFrom() {
    return airportFrom;
  }

  public String getAirportTo() {
    return airportTo;
  }

  public String getConnectingAirport() {
    return connectingAirport;
  }

  public Boolean getNewRoute() {
    return newRoute;
  }

  public Boolean getSeasonalRoute() {
    return seasonalRoute;
  }

  public String getOperator() {
    return operator;
  }

  public String getGroup() {
    return group;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("[airportFrom=").append(airportFrom)
      .append(", airportTo=").append(airportTo)
        // .append(", connectingAirport=").append(connectingAirport)
      .append("]")
      .toString();
  }
}
