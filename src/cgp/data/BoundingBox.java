package cgp.data;

import static cgp.data.Vec4.*;
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
   * @return When the ray intersects the bounding box or a negative value if it
   *         doesn't.
   */
  public double intersects(final Ray r, final TestCounter tc) {
    // we don't count empty boxes
    if(mins == null || maxs == null) return -1;
    tc.addBBoxCheck();
    // taken from
    // An Efficient and Robust Ray–Box Intersection Algorithm
    // Williams et al.
    final Vec4 d = r.getDirection();
    final boolean sx = d.getX() > 0;
    final boolean sy = d.getY() > 0;
    final boolean vx = d.getX() != 0;
    final boolean vy = d.getY() != 0;
    double tmin = r.hitCoord(sx ? mins : maxs, X);
    double tmax = r.hitCoord(!sx ? mins : maxs, X);
    final double tymin = r.hitCoord(sy ? mins : maxs, Y);
    final double tymax = r.hitCoord(!sy ? mins : maxs, Y);
    if(vx && vy && (tmin > tymax || tymin > tmax)) return -1;
    if(!vx || (vy && tymin > tmin)) {
      tmin = tymin;
    }
    if(!vx || (vy && tymax < tmax)) {
      tmax = tymax;
    }
    final boolean sz = d.getZ() > 0;
    final boolean vz = d.getZ() != 0;
    final double tzmin = r.hitCoord(sz ? mins : maxs, Z);
    final double tzmax = r.hitCoord(!sz ? mins : maxs, Z);
    if(vz && (vx || vy) && (tmin > tzmax || tzmin > tmax)) return -1;
    if((!vx && !vy) || (vz && tzmin > tmin)) {
      tmin = tzmin;
    }
    if((!vx && !vy) || (vz && tzmax < tmax)) {
      tmax = tzmax;
    }
    if(tmin < r.getFar() && tmax > r.getNear()) return Math.max(tmin, r.getNear());
    return -1;
  }

  /**
   * Getter.
   *
   * @param minX The minimal x.
   * @param minY The minimal y.
   * @param minZ The minimal z.
   * @return Gets the specified corner.
   */
  public Vec4 get(final boolean minX, final boolean minY, final boolean minZ) {
    return new Vec4(minX ? mins.getX() : maxs.getX(),
        minY ? mins.getY() : maxs.getY(),
            minZ ? mins.getZ() : maxs.getZ(), true);
  }

  /**
   * Getter.
   *
   * @return The center of the bounding box.
   */
  public Vec4 getCenter() {
    return maxs.add(mins).mul(0.5);
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
   * Getter.
   *
   * @return The width of the bounding box.
   */
  public double getWidth() {
    return maxs.getX() - mins.getX();
  }

  /**
   * Getter.
   *
   * @return The height of the bounding box.
   */
  public double getHeight() {
    return maxs.getY() - mins.getY();
  }

  /**
   * Getter.
   *
   * @return The depth of the bounding box.
   */
  public double getDepth() {
    return maxs.getZ() - mins.getZ();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[min: " + mins + " max: " + maxs + "]";
  }

}
