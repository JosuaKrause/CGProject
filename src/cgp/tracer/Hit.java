package cgp.tracer;

import java.util.Objects;

import cgp.data.Ray;
import cgp.data.Triangle;

/**
 * A triangle hit.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Hit {

  /** The ray. */
  private final Ray ray;
  /** The triangle that got hit or <code>null</code>. */
  private final Triangle tri;
  /**
   * The travel distance of the ray. This value is positive iff {@link #tri} is
   * not <code>null</code>.
   */
  private final double distance;
  /** The relative triangle test count. */
  private final double testCount;

  /**
   * Creates a new hit.
   * 
   * @param ray The ray.
   * @param tri The triangle that got hit or <code>null</code>.
   * @param distance The travel distance or a negative value if no triangle got
   *          hit. The travel distance is measured in direction vectors.
   * @param testCount The test count.
   * @param maxCount The total number of triangles.
   */
  public Hit(final Ray ray, final Triangle tri,
      final double distance, final TestCounter testCount, final int maxCount) {
    if((tri != null) != (distance > 0)) throw new IllegalArgumentException(
        "inconsistent input: " + tri + " " + distance);
    this.ray = Objects.requireNonNull(ray);
    this.tri = tri;
    this.distance = distance;
    this.testCount = (double) testCount.getCount() / maxCount;
  }

  /**
   * Getter.
   * 
   * @return The ray.
   */
  public Ray getRay() {
    return ray;
  }

  /**
   * Getter.
   * 
   * @return Whether there was a triangle hit.
   */
  public boolean hasHit() {
    return tri != null;
  }

  /**
   * Getter.
   * 
   * @return The triangle or <code>null</code>.
   */
  public Triangle getTriangle() {
    return tri;
  }

  /**
   * Getter.
   * 
   * @return The travel distance. Note that the distance is positive iff a
   *         triangle was hit.
   */
  public double getDistance() {
    return distance;
  }

  /**
   * Getter.
   * 
   * @return The relative number of performed tests.
   */
  public double getTestCount() {
    return testCount;
  }

}
