package cgp.data;

import java.util.Objects;

/**
 * Barycentric coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class BarycentricCoordinates {

  /** The triangle. */
  private final Triangle t;
  /** The position on the triangle. */
  private final Vec4 p;
  /** The u coordinate. */
  private final double u;
  /** The v coordinate. */
  private final double v;
  /** The w coordinate. */
  private final double w;

  /**
   * Creates barycentric coordinates.
   * 
   * @param t The triangle.
   * @param p The position on the triangle.
   * @param u The u coordinate.
   * @param v The v coordinate.
   * @param w The w coordinate.
   */
  BarycentricCoordinates(final Triangle t, final Vec4 p,
      final double u, final double v, final double w) {
    this.t = Objects.requireNonNull(t);
    this.p = Objects.requireNonNull(p);
    this.u = u;
    this.v = v;
    this.w = w;
    if(u < 0 || u > 1) throw new IllegalArgumentException("" + u);
    if(v < 0 || v > 1) throw new IllegalArgumentException("" + v);
    if(w < 0 || w > 1) throw new IllegalArgumentException("" + w);
  }

  /**
   * Getter.
   * 
   * @return The triangle.
   */
  public Triangle getTriangle() {
    return t;
  }

  /**
   * Getter.
   * 
   * @return The position on the triangle.
   */
  public Vec4 getPosition() {
    return p;
  }

  /**
   * Getter.
   * 
   * @return The u coordinate. This coordinate corresponds to the corner a.
   */
  public double getU() {
    return u;
  }

  /**
   * Getter.
   * 
   * @return The v coordinate. This coordinate corresponds to the corner b.
   */
  public double getV() {
    return v;
  }

  /**
   * Getter.
   * 
   * @return The w coordinate. This coordinate corresponds to the corner c.
   */
  public double getW() {
    return w;
  }

}
