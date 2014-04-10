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
    this.na = na.expectDirection().normalized();
    this.nb = nb.expectDirection().normalized();
    this.nc = nc.expectDirection().normalized();
  }

  /** A small value. */
  private static final double EPS = 1e-5;

  /**
   * Computes the traveled distance of the ray until it hit the triangle. The
   * test counter gets increased.
   * 
   * @param r The ray.
   * @param tc The test counter.
   * @return The squared traveled distance of the ray at the hit position or a
   *         negative value if the ray didn't hit the triangle in positive
   *         direction.
   */
  public double hit(final Ray r, final TestCounter tc) {
    tc.addCheck();
    final Vec4 negA = a.negate();
    final Vec4 edge1 = b.add(negA);
    final Vec4 edge2 = c.add(negA);
    final Vec4 dir = r.getDirection();
    final Vec4 p = dir.cross(edge2);
    final double det = edge1.prod(p);
    if(det > -EPS && det < EPS) return -1;
    final Vec4 t = r.getOrigin().add(negA);
    final double u = t.prod(p) / det;
    if(u < 0 || u > 1) return -1;
    final Vec4 q = t.cross(edge1);
    final double v = dir.prod(q) / det;
    if(v < 0 || v > 1) return -1;
    return edge2.prod(q) / det;
  }

  /**
   * Computes the normal of the triangle at the given position.
   * 
   * @param pos The position.
   * @return The normal.
   */
  public Vec4 getNormalAt(final Vec4 pos) {
    // TODO correct calculation
    final double distA = Math.sqrt(pos.getDistanceSq(a));
    final double distB = Math.sqrt(pos.getDistanceSq(b));
    final double distC = Math.sqrt(pos.getDistanceSq(c));
    final double totalDist = distA + distB + distC;
    return na.mul(1.0 - distA / totalDist).addMul(nb, 1.0 - distB / totalDist)
        .addMul(nc, 1.0 - distC / totalDist).normalized();
  }

}
