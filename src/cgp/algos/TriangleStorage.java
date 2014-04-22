package cgp.algos;

import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * A triangle storage data structure.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface TriangleStorage {

  /**
   * Adds a triangle.
   *
   * @param tri The triangle.
   */
  void addTriangle(Triangle tri);

  /**
   * This method is called after all triangles were added to the data structure.
   */
  void finishLoading();

  /**
   * Checks whether the ray hits a triangle.
   *
   * @param r The ray.
   * @param counter The check counter.
   * @return The hit.
   */
  Hit getHit(Ray r, TestCounter counter);

  /**
   * Getter.
   *
   * @return Iterates over all triangles.
   */
  Iterable<Triangle> getSoup();

  /**
   * Getter.
   *
   * @return The number of triangles.
   */
  int size();

}
