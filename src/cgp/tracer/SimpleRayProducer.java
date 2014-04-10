package cgp.tracer;

import cgp.data.Ray;
import cgp.data.Vec4;

/**
 * A simple ray producer.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleRayProducer implements RayProducer {

  /** The camera origin. */
  private final Vec4 origin;
  /** The viewing direction. */
  private final Vec4 center;
  /** The direction which is up for the camera. */
  private final Vec4 up;
  /** The direction which is left for the camera. */
  private final Vec4 left;
  /** The horizontal view angle in degrees. */
  private final double hAngle;
  /** The vertical view angle in degrees. */
  private final double vAngle;
  /** The width. */
  private final int w;
  /** The height. */
  private final int h;

  /**
   * Creates a simple ray producer.
   * 
   * @param origin The camera origin.
   * @param center The viewing direction.
   * @param up The direction which is up for the camera.
   * @param left The direction which is left for the camera.
   * @param w The width.
   * @param h The height.
   * @param hAngle The horizontal view angle in degrees.
   * @param vAngle The vertical view angle in degrees.
   */
  public SimpleRayProducer(final Vec4 origin, final Vec4 center,
      final Vec4 up, final Vec4 left, final int w, final int h,
      final double hAngle, final double vAngle) {
    this.w = w;
    this.h = h;
    this.origin = origin.expectPoint();
    this.center = center.expectDirection();
    this.up = up.expectDirection();
    this.left = left.expectDirection();
    this.hAngle = hAngle;
    this.vAngle = vAngle;
  }

  @Override
  public Ray getFor(final int x, final int y) {
    // TODO Auto-generated method stub
    return new Ray(origin, center);
  }

  @Override
  public int getWidth() {
    return w;
  }

  @Override
  public int getHeight() {
    return h;
  }

}
