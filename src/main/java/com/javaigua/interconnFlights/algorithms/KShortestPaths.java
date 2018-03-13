package com.javaigua.interconnFlights.algorithms;

import java.util.*;

/**
 * An algorithm to calculate the k-shortest paths in an edge weighted directed graph.
 *
 * A computer science text-book implementation.
 */
public class KShortestPaths {

  public static class Path implements Comparable<Path> {

    private Path previousPath;
    private DirectedEdge directedEdge;
    private int lastVertexInPath;
    private double weight;
    private HashSet<Integer> verticesInPath;

    public Path(int vertex) {
      lastVertexInPath = vertex;

      verticesInPath = new HashSet<>();
      verticesInPath.add(vertex);
    }

    public Path(Path previousPath, DirectedEdge directedEdge) {
      this(directedEdge.to());
      this.previousPath = previousPath;

      verticesInPath.addAll(previousPath.verticesInPath);

      this.directedEdge = directedEdge;
      weight += previousPath.weight() + directedEdge.weight();
    }

    public double weight() {
      return weight;
    }

    public Iterable<DirectedEdge> getPath() {
      LinkedList<DirectedEdge> path = new LinkedList<>();

      Path iterator = previousPath;

      while (iterator != null && iterator.directedEdge != null) {
        path.addFirst(iterator.directedEdge);

        iterator = iterator.previousPath;
      }
      path.add(directedEdge);

      return path;
    }

    @Override
    public int compareTo(Path other) {
      if (this.weight < other.weight) {
        return -1;
      } else if (this.weight > other.weight) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public static List<Path> getKShortestPaths(EdgeWeightedDigraph digraph, int source, int target, int kPaths) {
    List<Path> paths = new ArrayList<>();
    Map<Integer, Integer> countMap = new HashMap<>();
    countMap.put(target, 0);

    Deque<Path> priorityQueue = new ArrayDeque<>();
    priorityQueue.addLast(new Path(source));

    while (!priorityQueue.isEmpty() && countMap.get(target) < kPaths) {
      Path currentPath = priorityQueue.pollFirst();
      int lastVertexInPath = currentPath.lastVertexInPath;

      int pathsToCurrentVertex = 0;

      if (countMap.get(lastVertexInPath) != null) {
        pathsToCurrentVertex = countMap.get(lastVertexInPath);
      }

      pathsToCurrentVertex++;
      countMap.put(lastVertexInPath, pathsToCurrentVertex);

      if (lastVertexInPath == target) {
        paths.add(currentPath);
      }

      if (pathsToCurrentVertex <= kPaths) {
        for (DirectedEdge edge : digraph.adj(lastVertexInPath)) {
          if (!currentPath.verticesInPath.contains(edge.to())) {
            Path newPath = new Path(currentPath, edge);
            priorityQueue.addLast(newPath);
          }
        }
      }
    }
    return paths;
  }

}
