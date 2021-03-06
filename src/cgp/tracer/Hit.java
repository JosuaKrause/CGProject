package cgp.tracer;

import java.util.Objects;

import cgp.data.BarycentricCoordinates;
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
  /** The triangle test count. */
  private final long testCount;
  /** The bounding box test count. */
  private final long bboxCount;

  /**
   * Creates a failed hit.
   * 
   * @param ray The ray.
   * @param testCount The test count.
   */
  public Hit(final Ray ray, final TestCounter testCount) {
    this(ray, null, -1, testCount);
  }

  /**
   * Creates a new hit.
   * 
   * @param ray The ray.
   * @param tri The triangle that got hit or <code>null</code>.
   * @param distance The travel distance or a negative value if no triangle got
   *          hit. The travel distance is measured in direction vectors. If the
   *          triangle is <code>null</code> the distance is automatically set to
   *          a negative value.
   * @param testCount The test count.
   */
  public Hit(final Ray ray, final Triangle tri,
      final double distance, final TestCounter testCount) {
    this.ray = Objects.requireNonNull(ray);
    this.testCount = testCount.getCount();
    bboxCount = testCount.getBBoxCount();
    if(tri != null) {
      this.tri = tri;
      this.distance = distance;
    } else {
      this.tri = null;
      this.distance = -1;
    }
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

  /** The cache for the barycentric coordinate. */
  private BarycentricCoordinates bary;

  /**
   * Getter.
   * 
   * @return The barycentric coordinate of the hit.
   */
  public BarycentricCoordinates getBarycentric() {
    if(bary == null) {
      if(!hasHit()) throw new IllegalStateException("no hit");
      bary = tri.getAt(ray.getPosition(distance));
    }
    return bary;
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
   * @return The number of performed tests.
   */
  public long getTestCount() {
    return testCount;
  }

  /**
   * Getter.
   * 
   * @return The number of bounding box checks.
   */
  public long getBBoxCount() {
    return bboxCount;
  }

  /**
   * @return The hit's triangle.
   */
  public Triangle getTriangle() {
    return tri;
  }

}
