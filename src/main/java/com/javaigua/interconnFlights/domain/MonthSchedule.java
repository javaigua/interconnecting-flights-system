package com.javaigua.interconnFlights.domain;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A representation of the information of a MonthSchedule.
 */
public class MonthSchedule {
  private final Integer month;
  private final List<DaySchedule> days;

  public MonthSchedule() {
    this.month = 0;
    this.days = Collections.EMPTY_LIST;
  }

  public MonthSchedule(Integer month, List<DaySchedule> days) {
    this.month = month;
    this.days = days;
  }

  public Integer getMonth() {
    return month;
  }

  public List<DaySchedule> getDays() {
    return days;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("[month=").append(month)
      .append(", days=[").append(days.stream().map(Object::toString).collect(Collectors.joining(", ")))
      .append("]]")
      .toString();
  }
}
