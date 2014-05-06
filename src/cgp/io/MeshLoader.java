package cgp.io;

import java.io.IOException;

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
   * @throws IOException I/O Exception.
   */
  void loadMesh(TriangleStorage storage) throws IOException;

}
