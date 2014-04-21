package cgp.data;

import cgp.tracer.TestCounter;

public class BoundingBox {

  private final Vec4 mins;

  private final Vec4 maxs;

  public BoundingBox() {
    mins = null;
    maxs = null;
  }

  public BoundingBox(final Triangle t) {
    mins = min(min(t.getA(), t.getB()), t.getC());
    maxs = max(max(t.getA(), t.getB()), t.getC());
  }

  public BoundingBox(final Vec4 from, final Vec4 to) {
    mins = min(from, to);
    maxs = max(from, to);
  }

  private BoundingBox(final Vec4 min, final Vec4 max, final boolean processed) {
    mins = min;
    maxs = max;
  }

  public boolean contains(final Vec4 p) {
    if(mins == null || maxs == null) return false;
    p.expectPoint();
    for(int i = 0; i < 3; ++i) {
      if(mins.get(i) > p.get(i) || maxs.get(i) < p.get(i)) return false;
    }
    return true;
  }

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
    if(vz && (tmin > tzmax || tzmin > tmax)) return false;
    if((!vx && !vy) || (vz && tzmin > tmin)) {
      tmin = tzmin;
    }
    if((!vx && !vy) || (vz && tzmax < tmax)) {
      tmax = tzmax;
    }
    return tmin < r.getFar() && tmax > r.getNear();
  }

  public BoundingBox add(final BoundingBox o) {
    final Vec4 min = mins == null ? o.mins : o.mins == null ? mins : min(mins, o.mins);
    final Vec4 max = maxs == null ? o.maxs : o.maxs == null ? maxs : max(maxs, o.maxs);
    return new BoundingBox(min, max, true);
  }

  public static final Vec4 max(final Vec4 a, final Vec4 b) {
    a.expectPoint();
    b.expectPoint();
    return new Vec4(Math.max(a.getX(), b.getX()),
        Math.max(a.getY(), b.getY()),
        Math.max(a.getZ(), b.getZ()), true);
  }

  public static final Vec4 min(final Vec4 a, final Vec4 b) {
    a.expectPoint();
    b.expectPoint();
    return new Vec4(Math.min(a.getX(), b.getX()),
        Math.min(a.getY(), b.getY()),
        Math.min(a.getZ(), b.getZ()), true);
  }

}
