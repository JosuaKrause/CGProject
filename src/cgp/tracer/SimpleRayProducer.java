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
  private final Vec4 eye;
  /** The viewing direction. */
  private final Vec4 view;
  /** The direction which is up for the camera. */
  private final Vec4 up;
  /** The direction which is left for the camera. */
  private final Vec4 left;
  /** The field of view in degrees. */
  private final double fov;
  /** The width. */
  private final int w;
  /** The height. */
  private final int h;
  /** The camera depth. */
  private final double depth;

  /**
   * Creates a simple ray producer.
   * 
   * @param eye The camera origin.
   * @param view The viewing direction.
   * @param up The direction which is up for the camera.
   * @param left The direction which is left for the camera.
   * @param w The width.
   * @param h The height.
   * @param fov The field of view in degrees.
   * @param depth The camera depth.
   */
  public SimpleRayProducer(final Vec4 eye, final Vec4 view, final Vec4 up,
      final Vec4 left, final int w, final int h, final double fov, final double depth) {
    this.w = w;
    this.h = h;
    this.eye = eye.expectPoint();
    this.view = view.expectDirection();
    this.up = up.expectDirection();
    this.left = left.expectDirection();
    this.fov = fov;
    this.depth = depth;
  }

  @Override
  public Ray getFor(final int x, final int y) {
    final double angleX = -fov * ((double) x / w - 0.5);
    final double angleY = -fov * h / w * ((double) y / h - 0.5);
    final double lenLeft = Math.tan(Math.toRadians(angleX)) * depth;
    final double lenUp = Math.tan(Math.toRadians(angleY)) * depth;
    final Vec4 dir = view.mul(depth).addMul(left, lenLeft).addMul(up, lenUp).normalized();
    return new Ray(eye, dir);
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
