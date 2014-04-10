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
   * @return The total number of triangles.
   */
  int triangleCount();

}
