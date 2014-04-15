package cgp.io;

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
   */
  void loadMesh(TriangleStorage storage, AffineTransform4 aff);

}
