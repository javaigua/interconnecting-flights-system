package com.javaigua.interconnFlights.algorithms;

import com.javaigua.interconnFlights.domain.Flight;

/**
 * A weighted directed edge representation.
 *
 * A computer science text-book implementation by Robert Sedgewick and Kevin Wayne.
 */
public class DirectedEdge {
  private final int v;
  private final int w;
  private final double weight;
  private final Flight flight;

  public DirectedEdge(int v, int w, double weight, Flight flight) {
    if (v < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
    if (w < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
    if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
    this.v = v;
    this.w = w;
    this.weight = weight;
    this.flight = flight;
  }

  public int from() {
    return v;
  }

  public int to() {
    return w;
  }

  public double weight() {
    return weight;
  }

  public Flight flight() {
    return flight;
  }

  public String toString() {
    return new StringBuffer().append(v).append("->")
      .append(w).append(" ")
      .append(String.format("%5.2f", weight)).append(" ")
      .append(flight.toString())
      .toString();
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
