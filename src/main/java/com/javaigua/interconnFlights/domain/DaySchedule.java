package com.javaigua.interconnFlights.domain;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * A representation of the information of a DaySchedule.
 */
public class DaySchedule {
  private final Integer day;
  private final List<Flight> flights;

  public DaySchedule() {
    this.day = 0  ;
    this.flights = Collections.EMPTY_LIST;
  }

  public DaySchedule(Integer day, List<Flight> flights) {
    this.day = day;
    this.flights = flights;
  }

  public Integer getDay() {
    return day;
  }

  public List<Flight> getFlights() {
    return flights;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((day == null) ? 0 : day.hashCode());
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

    DaySchedule other = (DaySchedule) obj;
    if (day == null) {
      if (other.day != null)
        return false;
    } else if (!day.equals(other.day))
      return false;

    return true;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("[day=").append(day)
      .append(", flights=[").append(flights.stream().map(Object::toString).collect(Collectors.joining(", ")))
      .append("]]")
      .toString();
  }
}
