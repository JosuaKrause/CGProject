package cgp.data;

/**
 * A three dimensional vector using homogeneous coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Vec4 {

  /** The origin. */
  public static final Vec4 ORIGIN = new Vec4(0, 0, 0, 1);
  /** The zero vector. */
  public static final Vec4 NULL = new Vec4(0, 0, 0, 0);
  /** The x axis direction. */
  public static final Vec4 X_AXIS = new Vec4(1, 0, 0, 0);
  /** The y axis direction. */
  public static final Vec4 Y_AXIS = new Vec4(0, 1, 0, 0);
  /** The z axis direction. */
  public static final Vec4 Z_AXIS = new Vec4(0, 0, 1, 0);

  /** The x coordinate. */
  private final double x;
  /** The y coordinate. */
  private final double y;
  /** The z coordinate. */
  private final double z;
  /** The w coordinate. */
  protected final double w;

  /**
   * Creates a vector.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   * @param point Whether the vector is a point or a direction.
   */
  public Vec4(final double x, final double y, final double z, final boolean point) {
    this(x, y, z, point ? 1 : 0);
  }

  /**
   * Creates a vector.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   * @param w The w coordinate.
   */
  protected Vec4(final double x, final double y, final double z, final double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  /**
   * Getter.
   * 
   * @return The x coordinate.
   */
  public double getX() {
    return x;
  }

  /**
   * Getter.
   * 
   * @return The y coordinate.
   */
  public double getY() {
    return y;
  }

  /**
   * Getter.
   * 
   * @return The z coordinate.
   */
  public double getZ() {
    return z;
  }

  /**
   * Getter.
   * 
   * @return Whether the vector is a point.
   */
  public boolean isPoint() {
    return w != 0;
  }

  /**
   * Expects the vector to be a point.
   * 
   * @return The vector.
   */
  public Vec4 expectPoint() {
    if(!isPoint()) throw new IllegalArgumentException("expected point: " + this);
    return this;
  }

  /**
   * Expects the vector to be a direction.
   * 
   * @return The vector.
   */
  public Vec4 expectDirection() {
    if(isPoint()) throw new IllegalArgumentException("expected direction: " + this);
    return this;
  }

  /**
   * Getter.
   * 
   * @return The squared length of the vector.
   */
  public double lengthSq() {
    return x * x + y * y + z * z + w * w;
  }

  /**
   * Getter.
   * 
   * @return The negated vector.
   */
  public Vec4 negate() {
    return new Vec4(-x, -y, -z, -w);
  }

  /**
   * Scales the vector.
   * 
   * @param s The factor.
   * @return The scaled vector.
   */
  public Vec4 mul(final double s) {
    return new Vec4(x * s, y * s, z * s, w * s);
  }

  /**
   * Adds another vector.
   * 
   * @param o The other vector.
   * @return The sum vector.
   */
  public Vec4 add(final Vec4 o) {
    return new Vec4(x + o.x, y + o.y, z + o.z, w + o.w);
  }

  /**
   * Subtracts another vector.
   * 
   * @param o The other vector.
   * @return The difference vector.
   */
  public Vec4 sub(final Vec4 o) {
    return new Vec4(x - o.x, y - o.y, z - o.z, w - o.w);
  }

  /**
   * Adds another vector that is scaled.
   * 
   * @param o The other vector.
   * @param s The scaling of the other vector.
   * @return The vector representing <code>this + o*s</code>.
   */
  public Vec4 addMul(final Vec4 o, final double s) {
    return new Vec4(x + o.x * s, y + o.y * s, z + o.z * s, w + o.w * s);
  }

  /** The cache for the normalized vector. */
  private Vec4 norm;

  /**
   * Getter.
   * 
   * @return The normalized vector.
   */
  public Vec4 normalized() {
    if(norm == null) {
      final double sq = lengthSq();
      if(sq == 1) {
        norm = this;
      } else {
        norm = mul(1 / Math.sqrt(sq));
        norm.norm = norm;
      }
    }
    return norm;
  }

  /**
   * Computes the cross product with the given vector. Both vectors are assumed
   * to be directions.
   * 
   * @param o The other vector.
   * @return The cross product vector.
   */
  public Vec4 cross(final Vec4 o) {
    expectDirection();
    o.expectDirection();
    return new Vec4(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x,
        0);
  }

  /**
   * Computes the dot product of the vectors.
   * 
   * @param o The other vector.
   * @return The dot product.
   */
  public double dot(final Vec4 o) {
    expectDirection();
    o.expectDirection();
    return x * o.x + y * o.y + z * o.z;
  }

  /**
   * Rotates the direction around the x axis.
   * 
   * @param alpha The angle to rotate in radians.
   * @return The rotated vector.
   */
  public Vec4 rotateX(final double alpha) {
    expectDirection();
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new Vec4(x, cos * y - sin * z, sin * y + cos * z, false);
  }

  /**
   * Rotates the direction around the y axis.
   * 
   * @param alpha The angle to rotate in radians.
   * @return The rotated vector.
   */
  public Vec4 rotateY(final double alpha) {
    expectDirection();
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new Vec4(cos * x + sin * z, y, -sin * x + cos * z, false);
  }

  /**
   * Rotates the direction around the z axis.
   * 
   * @param alpha The angle to rotate in radians.
   * @return The rotated vector.
   */
  public Vec4 rotateZ(final double alpha) {
    expectDirection();
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new Vec4(cos * x - sin * y, sin * x + cos * y, z, false);
  }

  /**
   * Computes the angle enclosed by both vectors.
   * 
   * @param o The other vector.
   * @return The angle in radians.
   */
  public double angle(final Vec4 o) {
    return Math.asin(Math.sqrt(normalized().cross(o.normalized()).lengthSq()));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "[x: " + x + " y: " + y + " z: " + z + " w: " + w + "]";
  }

}
