package cgp.data;

import cgp.tracer.TestCounter;

/**
 * A triangle. It has three corners and three corresponding normal vectors.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Triangle {

  /** The first corner. */
  private final Vec4 a;
  /** The second corner. */
  private final Vec4 b;
  /** The third corner. */
  private final Vec4 c;
  /** The normal at the first corner. */
  private final Vec4 na;
  /** The normal at the second corner. */
  private final Vec4 nb;
  /** The normal at the third corner. */
  private final Vec4 nc;

  /**
   * Creates a triangle.
   * 
   * @param a The first corner.
   * @param b The second corner.
   * @param c The third corner.
   * @param na The normal at the first corner.
   * @param nb The normal at the second corner.
   * @param nc The normal at the third corner.
   */
  public Triangle(final Vec4 a, final Vec4 b, final Vec4 c,
      final Vec4 na, final Vec4 nb, final Vec4 nc) {
    this.a = a.expectPoint();
    this.b = b.expectPoint();
    this.c = c.expectPoint();
    this.na = na.expectDirection().getNormalized();
    this.nb = nb.expectDirection().getNormalized();
    this.nc = nc.expectDirection().getNormalized();
  }

  /**
   * Computes the traveled distance of the ray until it hit the triangle. The
   * test counter gets increased.
   * 
   * @param r The ray.
   * @param c The test counter.
   * @return The squared traveled distance of the ray at the hit position or a
   *         negative value if the ray didn't hit the triangle in positive
   *         direction.
   */
  public double hit(final Ray r, final TestCounter c) {
    // TODO
    return -1;
  }

  /**
   * Computes the normal of the triangle at the given position.
   * 
   * @param pos The position.
   * @return The normal.
   */
  public Vec4 getNormalAt(final Vec4 pos) {
    // TODO
    return null;
  }

}
