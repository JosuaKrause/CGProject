package cgp.algos;

import cgp.data.Triangle;

/**
 * A triangle storage data structure.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface TriangleStorage {

  /**
   * Adds a triangles to the storage.
   * 
   * @param t The triangle to add.
   */
  void addTriangle(Triangle t);

}
