package cgp.data;

/**
 * A ray.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Ray {

  /** The origin of the ray. */
  private final Vec4 origin;
  /** The normalized direction of the ray. */
  private final Vec4 dir;
  /** The minimal distance the ray needs to travel. */
  private final double min;
  /** The maximal distance the ray can travel. */
  private final double max;

  /**
   * Creates a new ray.
   * 
   * @param origin The origin.
   * @param dir The direction.
   * @param min The minimal distance the ray needs to travel.
   * @param max The maximal distance the ray can travel.
   */
  public Ray(final Vec4 origin, final Vec4 dir, final double min, final double max) {
    this.origin = origin.expectPoint();
    this.dir = dir.expectDirection().normalized();
    this.min = min;
    this.max = max;
  }

  /**
   * Getter.
   * 
   * @return The view direction.
   */
  public Vec4 getDirection() {
    return dir;
  }

  /**
   * Getter.
   * 
   * @return The origin.
   */
  public Vec4 getOrigin() {
    return origin;
  }

  /**
   * Getter.
   * 
   * @param at The traveled distance.
   * @return The position at the traveled distance.
   */
  public Vec4 getPosition(final double at) {
    return origin.addMul(dir, at);
  }

  /**
   * Getter.
   * 
   * @return The minimal distance the ray needs to travel.
   */
  public double getNear() {
    return min;
  }

  /**
   * Getter.
   * 
   * @return The maximal distance the ray can travel.
   */
  public double getFar() {
    return max;
  }

  /**
   * Tests whether the given distance is valid.
   * 
   * @param d The distance.
   * @return Whether the distance is in the correct range.
   */
  public boolean isValidDistance(final double d) {
    return d > min && d < max;
  }

}
