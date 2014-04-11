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
   * Creates a triangle and computes the normals accordingly. The points need to
   * be in counter clock-wise order.
   * 
   * @param a The first corner.
   * @param b The second corner.
   * @param c The third corner.
   */
  public Triangle(final Vec4 a, final Vec4 b, final Vec4 c) {
    this.a = a.expectPoint();
    this.b = b.expectPoint();
    this.c = c.expectPoint();
    nc = nb = na = b.sub(a).cross(c.sub(a)).normalized();
  }

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
    final Vec4 edge1 = b.sub(a);
    final Vec4 edge2 = c.sub(a);
    edge1.expectDirection();
    edge2.expectDirection();
    final Vec4 norm = edge1.cross(edge2);
    norm.expectDirection();
    final Vec4 dir = r.getDirection();
    dir.expectDirection();
    final double det = dir.prod(norm);
    if(det > -EPS && det < EPS) return -1;
    final Vec4 oa = a.sub(r.getOrigin());
    oa.expectDirection();
    final double pos = oa.prod(norm) / det;
    if(pos <= 0) return -1;
    final Vec4 p = r.getPosition(pos);
    p.expectPoint();
    final double u = norm.prod(c.sub(b).cross(p.sub(b)));
    if(u < 0) return -1;
    final double v = norm.prod(a.sub(c).cross(p.sub(c)));
    if(v < 0) return -1;
    final double w = norm.prod(edge1.cross(p.sub(a)));
    if(w < 0) return -1;
    return pos;
  }

  /**
   * Computes the normal of the triangle at the given position.
   * 
   * @param p The position.
   * @return The normal.
   */
  public Vec4 getNormalAt(final Vec4 p) {
    final double total = b.sub(a).cross(c.sub(a)).lengthSq();
    final double da = Math.sqrt(b.sub(p).cross(c.sub(p)).lengthSq() / total);
    final double db = Math.sqrt(a.sub(p).cross(c.sub(p)).lengthSq() / total);
    // final double dc = Math.sqrt(a.sub(p).cross(b.sub(p)).lengthSq() / total);
    final double dc = 1 - da - db;
    return na.mul(1.0 - da).addMul(nb, 1.0 - db).addMul(nc, 1.0 - dc).normalized();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[a: " + a + " b: " + b + " c: " + c + "]";
  }

}
