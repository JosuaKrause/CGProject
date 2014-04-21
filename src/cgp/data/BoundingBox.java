package cgp.data;

import cgp.tracer.TestCounter;

/**
 * A bounding box in three dimensions.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class BoundingBox {

  /** The minimal values. */
  private final Vec4 mins;
  /** The maximal values. */
  private final Vec4 maxs;

  /** An empty bounding box. */
  public BoundingBox() {
    mins = null;
    maxs = null;
  }

  /**
   * Computes the bounding box of a triangle.
   *
   * @param t The triangle.
   */
  public BoundingBox(final Triangle t) {
    mins = min(min(t.getA(), t.getB()), t.getC());
    maxs = max(max(t.getA(), t.getB()), t.getC());
  }

  /**
   * Computes the bounding box of the given vectors.
   *
   * @param from One vector.
   * @param to Another vector.
   */
  public BoundingBox(final Vec4 from, final Vec4 to) {
    mins = min(from, to);
    maxs = max(from, to);
  }

  /**
   * Sets the bounding box.
   *
   * @param min The minimal values.
   * @param max The maximal values.
   * @param processed This argument is solely used to distinguish the
   *          constructor.
   */
  private BoundingBox(final Vec4 min, final Vec4 max,
      @SuppressWarnings("unused") final boolean processed) {
    mins = min;
    maxs = max;
  }

  /**
   * Checks whether the given vector is contained in the bounding box. Edges are
   * included in the bounding box.
   *
   * @param p The point.
   * @return Whether the given vector is contained in the bounding box.
   */
  public boolean contains(final Vec4 p) {
    if(mins == null || maxs == null) return false;
    p.expectPoint();
    for(int i = 0; i < 3; ++i) {
      if(mins.get(i) > p.get(i) || maxs.get(i) < p.get(i)) return false;
    }
    return true;
  }

  /**
   * Checks whether the given ray intersects the bounding box.
   *
   * @param r The ray.
   * @param tc The test counter.
   * @return Whether the ray intersects the bounding box.
   */
  public boolean intersects(final Ray r, final TestCounter tc) {
    if(mins == null || maxs == null) return false;
    tc.addBBoxCheck(); // we don't count empty boxes
    // taken from
    // An Efficient and Robust Ray–Box Intersection Algorithm
    // Williams et al.
    final Vec4 d = r.getDirection();
    final boolean sx = d.getX() > 0;
    final boolean sy = d.getY() > 0;
    final boolean vx = d.getX() != 0;
    final boolean vy = d.getY() != 0;
    double tmin = r.hitCoord(sx ? mins : maxs, Vec4.X);
    double tmax = r.hitCoord(!sx ? mins : maxs, Vec4.X);
    final double tymin = r.hitCoord(sy ? mins : maxs, Vec4.Y);
    final double tymax = r.hitCoord(!sy ? mins : maxs, Vec4.Y);
    if(vx && vy && (tmin > tymax || tymin > tmax)) return false;
    if(!vx || (vy && tymin > tmin)) {
      tmin = tymin;
    }
    if(!vx || (vy && tymax < tmax)) {
      tmax = tymax;
    }
    final boolean sz = d.getZ() > 0;
    final boolean vz = d.getZ() != 0;
    final double tzmin = r.hitCoord(sz ? mins : maxs, Vec4.Z);
    final double tzmax = r.hitCoord(!sz ? mins : maxs, Vec4.Z);
    if(vz && (vx || vy) && (tmin > tzmax || tzmin > tmax)) return false;
    if((!vx && !vy) || (vz && tzmin > tmin)) {
      tmin = tzmin;
    }
    if((!vx && !vy) || (vz && tzmax < tmax)) {
      tmax = tzmax;
    }
    return tmin < r.getFar() && tmax > r.getNear();
  }

  /**
   * Adds both bounding boxes.
   *
   * @param o The other bounding box.
   * @return The resulting bounding box. Empty bounding boxes are handled
   *         correctly.
   */
  public BoundingBox add(final BoundingBox o) {
    final Vec4 min = mins == null ? o.mins : o.mins == null ? mins : min(mins, o.mins);
    final Vec4 max = maxs == null ? o.maxs : o.maxs == null ? maxs : max(maxs, o.maxs);
    return new BoundingBox(min, max, true);
  }

  /**
   * Computes the componnet maximum of the given vectors.
   *
   * @param a The first vector.
   * @param b The second vector.
   * @return The maximum for each component.
   */
  public static final Vec4 max(final Vec4 a, final Vec4 b) {
    a.expectPoint();
    b.expectPoint();
    return new Vec4(Math.max(a.getX(), b.getX()),
        Math.max(a.getY(), b.getY()),
        Math.max(a.getZ(), b.getZ()), true);
  }

  /**
   * Computes the componnet minimum of the given vectors.
   *
   * @param a The first vector.
   * @param b The second vector.
   * @return The minimum for each component.
   */
  public static final Vec4 min(final Vec4 a, final Vec4 b) {
    a.expectPoint();
    b.expectPoint();
    return new Vec4(Math.min(a.getX(), b.getX()),
        Math.min(a.getY(), b.getY()),
        Math.min(a.getZ(), b.getZ()), true);
  }

}
