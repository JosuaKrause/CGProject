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
  /** The field of view in degrees. */
  private final double fov;
  /** The width. */
  private final int w;
  /** The height. */
  private final int h;
  /** The nearest distance. */
  private final double near;
  /** The farthest distance. */
  private final double far;

  /**
   * Creates a simple ray producer.
   * 
   * @param eye The camera origin.
   * @param view The viewing direction.
   * @param up The direction which is up for the camera.
   * @param w The width.
   * @param h The height.
   * @param fov The field of view in degrees.
   * @param near Nearest distance.
   * @param far Farthest distance.
   */
  public SimpleRayProducer(final Vec4 eye, final Vec4 view, final Vec4 up,
      final int w, final int h, final double fov, final double near, final double far) {
    this.w = w;
    this.h = h;
    this.eye = eye.expectPoint();
    this.view = view.expectDirection();
    this.up = up.expectDirection();
    this.fov = fov;
    this.near = near;
    this.far = far;
  }

  @Override
  public Ray getFor(final int x, final int y) {
    final double angleX = fov * ((double) x / w - 0.5);
    final double angleY = -fov * h / w * ((double) y / h - 0.5);
    final double lenLeft = Math.tan(Math.toRadians(angleX));
    final double lenUp = Math.tan(Math.toRadians(angleY));
    final Vec4 left = view.cross(up);
    final Vec4 dir = view.addMul(left, lenLeft).addMul(up, lenUp).normalized();
    return new Ray(eye.addMul(dir, near), dir, far - near);
  }

  @Override
  public int getWidth() {
    return w;
  }

  @Override
  public int getHeight() {
    return h;
  }

  @Override
  public double getFov() {
    return fov;
  }

  @Override
  public Vec4 getEye() {
    return eye;
  }

  @Override
  public Vec4 getView() {
    return view;
  }

  @Override
  public Vec4 getUp() {
    return up;
  }

  @Override
  public double getNear() {
    return near;
  }

  @Override
  public double getFar() {
    return far;
  }

}
