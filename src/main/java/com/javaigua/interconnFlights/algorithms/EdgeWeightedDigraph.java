package com.javaigua.interconnFlights.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An edge weighted directed graph.
 *
 * A computer science text-book implementation by Robert Sedgewick and Kevin Wayne.
 */
public class EdgeWeightedDigraph {

  private final int V;
  private int E;
  private List<DirectedEdge>[] adj;
  private int[] indegree;

  public EdgeWeightedDigraph(int V) {
    if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");
    this.V = V;
    this.E = 0;
    this.indegree = new int[V];
    adj = (List<DirectedEdge>[]) new List[V];
    for (int v = 0; v < V; v++)
      adj[v] = new ArrayList<>();
  }

  public EdgeWeightedDigraph(EdgeWeightedDigraph G) {
    this(G.V());
    this.E = G.E();
    for (int v = 0; v < G.V(); v++)
      this.indegree[v] = G.indegree(v);
    for (int v = 0; v < G.V(); v++) {
      // reverse so that adjacency list is in same order as original
      Deque<DirectedEdge> reverse = new ArrayDeque<>();
      for (DirectedEdge e : G.adj[v]) {
        reverse.push(e);
      }
      for (DirectedEdge e : reverse) {
        adj[v].add(e);
      }
    }
  }

  public int V() {
    return V;
  }

  public int E() {
    return E;
  }

  public void addEdge(DirectedEdge e) {
    int v = e.from();
    int w = e.to();
    adj[v].add(e);
    indegree[w]++;
    E++;
  }

  public Iterable<DirectedEdge> adj(int v) {
    return adj[v];
  }

  public int outdegree(int v) {
    return adj[v].size();
  }

  public int indegree(int v) {
    return indegree[v];
  }

  public Iterable<DirectedEdge> edges() {
    List<DirectedEdge> list = new ArrayList<>();
    for (int v = 0; v < V; v++) {
      for (DirectedEdge e : adj(v)) {
        list.add(e);
      }
    }
    return list;
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("{vC: ").append(V).append(", eC: ").append(E).append(", v:[");
    for (int v = 0; v < V; v++) {
      s.append(v).append(": {");
      for (DirectedEdge e : adj[v]) {
        s.append(e).append("  ");
      }
      s.append("} ");
      if(v + 1 < V) s.append(", ");
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