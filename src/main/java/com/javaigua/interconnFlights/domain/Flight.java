package com.javaigua.interconnFlights.domain;

import java.time.LocalTime;

/**
 * A representation of the information of a Flight.
 */
public class Flight {
  private final String number;
  private final String departureTime;
  private final String arrivalTime;

  public Flight() {
    this.number = "";
    this.departureTime = "";
    this.arrivalTime = "";
  }

  public Flight(String number, String departureTime, String arrivalTime) {
    this.number = number;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
  }

  public String getNumber() {
    return number;
  }

  public String getDepartureTime() {
    return departureTime;
  }

  public String getArrivalTime() {
    return arrivalTime;
  }

  public LocalTime getDepartureLocalTime() {
    return LocalTime.parse(departureTime);
  }

  public LocalTime getArrivalLocalTime() {
    return LocalTime.parse(arrivalTime);
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

    Flight other = (Flight) obj;
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
      .append(", departureTime=").append(departureTime)
      .append(", arrivalTime=").append(arrivalTime)
      .append("]")
      .toString();
  }
}
