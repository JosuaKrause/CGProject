package cgp.algos;

import java.util.ArrayList;
import java.util.Collections;
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
  public void finishLoading() {
    // nothing to do
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    double minDist = Double.POSITIVE_INFINITY;
    Triangle curBest = null;
    for(final Triangle t : triangles) {
      final double dist = t.hit(r, c);
      if(r.isValidDistance(dist) && dist < minDist) {
        minDist = dist;
        curBest = t;
      }
    }
    return new Hit(r, curBest, minDist, c, this);
  }

  @Override
  public Iterable<Triangle> getSoup() {
    return Collections.unmodifiableCollection(triangles);
  }

  @Override
  public int triangleCount() {
    return triangles.size();
  }

  @Override
  public int bboxCount() {
    return 1;
  }

}
