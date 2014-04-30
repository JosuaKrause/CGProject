package cgp.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    return new Hit(r, curBest, minDist, c);
  }

  @Override
  public Iterable<Triangle> getSoup() {
    return Collections.unmodifiableCollection(triangles);
  }

  /**
   * Getter.
   *
   * @param index The index.
   * @return The triangle at the given position.
   */
  protected Triangle getTriangle(final int index) {
    return triangles.get(index);
  }

  /**
   * Sorts the triangles.
   * 
   * @param cmp The comparison.
   */
  protected void sort(final Comparator<Triangle> cmp) {
    Collections.sort(triangles, cmp);
  }

  @Override
  public int size() {
    return triangles.size();
  }

}
