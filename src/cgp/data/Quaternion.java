package cgp.data;

/**
 * A quaternion view on a vector.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class Quaternion extends Vec4 {

  /**
   * Creates a quaternion.
   *
   * @param vec The vector.
   * @param s The scalar angle.
   */
  public Quaternion(final Vec4 vec, final double s) {
    super(vec.getX(), vec.getY(), vec.getZ(), s);
    vec.expectDirection();
  }

  /**
   * Creates a quaternion.
   *
   * @param x The x component.
   * @param y The y component.
   * @param z The z component.
   * @param s The scalar angle.
   */
  protected Quaternion(final double x, final double y, final double z, final double s) {
    super(x, y, z, s);
  }

  @Override
  public Quaternion negate() {
    return new Quaternion(-getX(), -getY(), -getZ(), getS());
  }

  /**
   * Getter.
   *
   * @return Converts back to a vector.
   */
  public Vec4 getVec() {
    return new Vec4(getX(), getY(), getZ(), 0);
  }

  /**
   * Getter.
   *
   * @return The scalar angle.
   */
  public double getS() {
    return w;
  }

  /**
   * Multiplies this quaternion with another quaternion.
   *
   * @param o The other quaternion.
   * @return The multiplied quaternion.
   */
  public Quaternion mul(final Quaternion o) {
    final double s1 = getS();
    final double x1 = getX();
    final double y1 = getY();
    final double z1 = getZ();
    final double s2 = o.getS();
    final double x2 = o.getX();
    final double y2 = o.getY();
    final double z2 = o.getZ();
    return new Quaternion(s1 * x2 + s2 * x1 + y1 * z2 - y2 * z1,
        s1 * y2 + s2 * y1 + z1 * x2 - z2 * x1,
        s1 * z2 + s2 * z1 + x1 * y2 - x2 * y1,
        s1 * s2 - x1 * x2 - y1 * y2 - z1 * z2);
  }

  /**
   * Creates a normalized quaternion.
   *
   * @param alpha The scalar angle.
   * @param axis The axis.
   * @return The quaternion.
   */
  public static final Quaternion normQuaternion(final double alpha, final Vec4 axis) {
    axis.expectDirection();
    final double a = alpha * 0.5;
    return new Quaternion(axis.normalized().mul(Math.sin(a)), Math.cos(a));
  }

  public static final Quaternion rotate(final Vec4 v, final Vec4 axis, final double theta) {
    return rotate(v, normQuaternion(theta, axis));
  }

  public static final Quaternion rotate(final Vec4 v, final Quaternion p) {
    return rotate(new Quaternion(v, 0), p);
  }

  public static final Quaternion rotate(final Quaternion v, final Quaternion p) {
    final Quaternion q = p.negate();
    return q.mul(v).mul(p);
  }

}
