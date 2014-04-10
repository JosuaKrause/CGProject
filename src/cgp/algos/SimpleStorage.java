package cgp.algos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * A simple triangle storage strategy. Every triangle gets tested for every ray.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleStorage implements TriangleStorage {

  /** The list of triangles. */
  private final List<Triangle> triangles = new ArrayList<>();

  @Override
  public void addTriangle(final Triangle tri) {
    triangles.add(Objects.requireNonNull(tri));
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    double minDist = Double.POSITIVE_INFINITY;
    Triangle curBest = null;
    for(final Triangle t : triangles) {
      final double dist = t.hit(r, c);
      if(dist > 0 && dist < minDist) {
        minDist = dist;
        curBest = t;
      }
    }
    if(curBest == null) return new Hit(r, null, -1, c, triangleCount());
    return new Hit(r, curBest, Math.sqrt(minDist), c, triangleCount());
  }

  @Override
  public int triangleCount() {
    return triangles.size();
  }

}
