package cgp.io;

import cgp.algos.TriangleStorage;

/**
 * Loads a mesh into a triangle storage.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface MeshLoader {

  /**
   * Loads a mesh into the storage.
   * 
   * @param storage The storage.
   */
  void loadMesh(TriangleStorage storage);

}
