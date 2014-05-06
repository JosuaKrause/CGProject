package cgp.algos;

import cgp.data.Ray;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * Tests for triangle hits.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class Hitter {

  /** The triangles. */
  protected Triangles ts;

  /**
   * Fills the data structure with the triangles.
   * 
   * @param ts The triangles.
   */
  public void fromTriangles(final Triangles ts) {
    this.ts = ts;
    build();
  }

  /** Builds the data structure. */
  protected abstract void build();

  /**
   * Checks whether the ray hits a triangle.
   *
   * @param r The ray.
   * @param counter The check counter.
   * @return The hit.
   */
  public abstract Hit getHit(Ray r, TestCounter counter);

}
