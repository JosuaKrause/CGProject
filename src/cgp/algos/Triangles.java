package cgp.algos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cgp.data.AffineTransform4;
import cgp.data.Triangle;
import cgp.io.MeshLoader;

/**
 * Stores the triangles.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class Triangles {

  /** The list of triangles. */
  private List<Triangle> triangles;

  /**
   * Sets the triangles in the list.
   *
   * @param loader The mesh loader.
   * @param aff The affine transformation.
   * @throws IOException I/O Exception.
   */
  public void setTriangles(final MeshLoader loader, final AffineTransform4 aff)
      throws IOException {
    final List<Triangle> triangles = new ArrayList<>();
    loader.loadMesh(new TriangleStorage() {

      @Override
      public void addTriangle(final Triangle t) {
        triangles.add(aff.transform(t));
      }

    });
    this.triangles = triangles;
  }

  /**
   * Getter.
   *
   * @return A list of all triangles.
   */
  public List<Triangle> getList() {
    if(triangles == null) return Collections.emptyList();
    return Collections.unmodifiableList(triangles);
  }

  /**
   * Getter.
   *
   * @param index The index.
   * @return The triangle at the given position.
   */
  protected Triangle getTriangle(final int index) {
    if(triangles == null) throw new IndexOutOfBoundsException("" + index);
    return triangles.get(index);
  }

  /**
   * Getter.
   *
   * @return The number of triangles.
   */
  public int size() {
    if(triangles == null) return 0;
    return triangles.size();
  }

}
