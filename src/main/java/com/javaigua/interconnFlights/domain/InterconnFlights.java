package com.javaigua.interconnFlights.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A representation of the information of InterconnFlights.
 */
@JsonPropertyOrder({ "id", "stops", "legs" })
public class InterconnFlights {
  private String id;
  private final Integer stops;
  private final InterconnFlight[] legs;


  public InterconnFlights() {
    this.stops = 0;
    this.legs = new InterconnFlight[0];
  }

  public InterconnFlights(String id, InterconnFlight[] legs) {
    this.id = id;
    this.stops = legs != null ? legs.length : 0;
    this.legs = legs;
  }

  @JsonIgnore
  public String getId() {
    return id;
  }

  public Integer getStops() {
    return legs != null ? legs.length : 0;
  }

  public InterconnFlight[] getLegs() {
    return legs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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

    InterconnFlights other = (InterconnFlights) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;

    return true;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("[stops=").append(stops)
      .append(", legs=").append(legs)
      .append("]")
      .toString();
  }
}
