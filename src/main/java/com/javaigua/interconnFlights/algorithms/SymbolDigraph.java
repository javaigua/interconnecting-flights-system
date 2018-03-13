package com.javaigua.interconnFlights.algorithms;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.javaigua.interconnFlights.domain.*;

/**
 * A symbol table for a graph of airports IATA codes (vertices) and flights between them (edges).
 *
 * This table maps IATA code names to integer ids given in sequence to every airport contained in the provided
 * list of routes. The connections between them are weighted directed edges created by the schedule information that
 * link them together.
 *
 * A computer science text-book implementation by Robert Sedgewick and Kevin Wayne.
 */
public class SymbolDigraph {
  private Map<String, Integer> st;
  private String[] keys;
  private EdgeWeightedDigraph graph;

  public SymbolDigraph(Map<String, Route> routes, Map<String, List<MonthSchedule>> schedules) {
    st = new HashMap<>();

    // First pass builds the index by reading strings to associate distinct strings with an index
    for (Route route : routes.values()) {
      String[] a = new String[]{route.getAirportFrom(), route.getAirportTo()};
      for (int i = 0; i < a.length; i++) {
        if (!st.containsKey(a[i]))
          st.put(a[i], st.size());
      }
    }

    // inverted index to get string keys in an array
    keys = new String[st.size()];
    for (String name : st.keySet()) {
      keys[st.get(name)] = name;
    }

    // second pass builds the digraph by connecting first vertex on each line to all others
    graph = new EdgeWeightedDigraph(st.size());
    for (Route route : routes.values()) {

      int v = st.get(route.getAirportFrom());
      int w = st.get(route.getAirportTo());
      String routeKey = getKeyFor(route.getAirportFrom(), route.getAirportTo());

      // we only care for routes with available schedule
      if (schedules.containsKey(routeKey)) {
        for (MonthSchedule monthSchedule : schedules.get(routeKey)) {
          for (DaySchedule daySchedule : monthSchedule.getDays()) {
            for (Flight flight : daySchedule.getFlights()) {
              // weight is flight duration in minutes
              double weight = 0D;
              // same date departure and arrival weight calculation
              if (flight.getDepartureLocalTime().isBefore(flight.getArrivalLocalTime())) {
                weight = Double.valueOf(MINUTES.between(flight.getDepartureLocalTime(), flight.getArrivalLocalTime()));
              } else {
                // different date depature and arrival weight calculation
                weight += Double.valueOf(MINUTES.between(flight.getDepartureLocalTime(), LocalTime.of(23, 59, 59)));
                weight += Double.valueOf(MINUTES.between(LocalTime.of(0, 0, 0), flight.getArrivalLocalTime()));
              }
              graph.addEdge(new DirectedEdge(v, w, weight, flight));
            }
          }
        }
      }
    }
  }

  public boolean contains(String s) {
    return st.containsKey(s);
  }

  public int indexOf(String s) {
    return st.get(s);
  }


  public String nameOf(int v) {
    return keys[v];
  }

  public EdgeWeightedDigraph digraph() {
    return graph;
  }

  private static String getKeyFor(String source, String destination) {
    return source + "_" + destination;
  }

  public String toString() {
    EdgeWeightedDigraph g = digraph();
    StringBuilder s = new StringBuilder();
    s.append("{vC: ").append(g.V()).append(", eC: ").append(g.E()).append(", v:[");
    for (int v = 0; v < g.V(); v++) {
      s.append(v).append(": {");
      for (DirectedEdge e : g.adj(v)) {
        s.append(nameOf(e.from())).append("->")
          .append(nameOf(e.to())).append(" ")
          .append(String.format("%5.2f", e.weight())).append(" ")
          .append(e.flight().getNumber())
          .append(" ");
      }
      s.append("} ");
      if (v + 1 < g.V()) s.append(", ");
    }
    return s.append("}}").toString();
  }

}

/******************************************************************************
 *  Copyright 2002-2016, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/