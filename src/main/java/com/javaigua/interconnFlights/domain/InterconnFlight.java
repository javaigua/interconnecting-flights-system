package com.javaigua.interconnFlights.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A representation of the information of a InterconnFlight.
 */
@JsonPropertyOrder({ "number", "departureAirport", "arrivalAirport", "departureDateTime", "arrivalDateTime" })
public class InterconnFlight {
  private final String number;
  private final String departureAirport;
  private final String arrivalAirport;
  private final String departureDateTime;
  private final String arrivalDateTime;

  public InterconnFlight() {
    this.number = "";
    this.departureAirport = "";
    this.arrivalAirport = "";
    this.departureDateTime = "";
    this.arrivalDateTime = "";
  }

  public InterconnFlight(String number, String departureAirport, String arrivalAirport, String departureDateTime,
                         String arrivalDateTime) {
    this.number = number;
    this.departureAirport = departureAirport;
    this.arrivalAirport = arrivalAirport;
    this.departureDateTime = departureDateTime;
    this.arrivalDateTime = arrivalDateTime;
  }

  @JsonIgnore
  public String getNumber() {
    return number;
  }

  public String getDepartureAirport() {
    return departureAirport;
  }

  public String getArrivalAirport() {
    return arrivalAirport;
  }

  public String getDepartureDateTime() {
    return departureDateTime;
  }

  public String getArrivalDateTime() {
    return arrivalDateTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((number == null) ? 0 : number.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;

    InterconnFlight other = (InterconnFlight) obj;
    if (number == null) {
      if (other.number != null)
        return false;
    } else if (!number.equals(other.number))
      return false;

    return true;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("[number=").append(number)
      .append(", departureAirport=").append(departureAirport)
      .append(", arrivalAirport=").append(arrivalAirport)
      .append(", departureDateTime=").append(departureDateTime)
      .append(", arrivalDateTime=").append(arrivalDateTime)
      .append("]")
      .toString();
  }
}
