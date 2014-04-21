package cgp.io;

import java.io.IOException;

import cgp.algos.TriangleStorage;
import cgp.data.AffineTransform4;

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
   * @param aff The affine transformation to apply before adding the triangles.
   * @throws IOException I/O Exception.
   */
  void loadMesh(TriangleStorage storage, AffineTransform4 aff) throws IOException;

}
