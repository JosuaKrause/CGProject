package cgp.tracer;

import cgp.data.Quaternion;
import cgp.data.Ray;
import cgp.data.Vec4;

/**
 * A simple ray producer.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleRayProducer implements RayProducer {

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
  /** The camera origin. */
  private Vec4 eye;
  /** The viewing direction. */
  private Vec4 view;
  /** The direction which is up for the camera. */
  private Vec4 up;

  /**
   * Creates a simple ray producer.
   *
   * @param w The width.
   * @param h The height.
   * @param fov The field of view in degrees.
   * @param near Nearest distance.
   * @param far Farthest distance.
   */
  public SimpleRayProducer(final int w, final int h, final double fov,
      final double near, final double far) {
    this.w = w;
    this.h = h;
    this.fov = fov;
    this.near = near;
    this.far = far;
    eye = Vec4.ORIGIN;
    view = Vec4.Z_AXIS.negate();
    up = Vec4.Y_AXIS;
  }

  @Override
  public void setView(final Vec4 eye, final Vec4 view, final Vec4 up) {
    this.eye = eye.expectPoint();
    this.view = view.expectDirection();
    this.up = up.expectDirection();
  }

  @Override
  public void move(final boolean forward, final boolean ortho, final double amount) {
    final Vec4 move = ortho ? getLeft() : view;
    eye = eye.addMul(move, forward ? amount : -amount);
  }

  /** The rotation step angle. */
  private static final double ROT_STEP = Math.PI / 512.0;

  @Override
  public void rotateByTicks(final int dx, final int dy) {
    final double angleX = -dx * ROT_STEP;
    final double angleY = -dy * ROT_STEP;
    rotate(angleX, angleY);
  }

  /**
   * Rotates the view using quaternions.
   *
   * @param angleX The angle in x direction.
   * @param angleY The angle in y direction.
   */
  public void rotate(final double angleX, final double angleY) {
    final Quaternion aUp = Quaternion.normQuaternion(-angleX, Vec4.Y_AXIS);
    final Quaternion aLeft = Quaternion.normQuaternion(-angleY, getLeft());
    final Quaternion v = Quaternion.rotate(Quaternion.rotate(view, aUp), aLeft);
    final Quaternion u = Quaternion.rotate(Quaternion.rotate(up, aUp), aLeft);
    view = v.getVec().normalized();
    up = u.getVec().normalized();
    leftCache = null;
  }

  @Override
  public void rotateViewByTicks(final int ticks) {
    rotateView(ticks * ROT_STEP);
  }

  /**
   * Rotates around the view direction.
   *
   * @param angle The angle.
   */
  public void rotateView(final double angle) {
    final Quaternion qV = Quaternion.normQuaternion(angle, view);
    final Quaternion pV = qV.negate();
    final Quaternion u = new Quaternion(up, 0);
    up = qV.mul(u).mul(pV).getVec().normalized();
    leftCache = null;
  }

  @Override
  public Ray getFor(final int x, final int y) {
    final double angleX = -fov * w / h * ((double) x / w - 0.5);
    final double angleY = -fov * ((double) y / h - 0.5);
    final double lenLeft = Math.tan(Math.toRadians(angleX));
    final double lenUp = Math.tan(Math.toRadians(angleY));
    final Vec4 left = getLeft();
    final Vec4 dir = view.addMul(left, lenLeft).addMul(up, lenUp).normalized();
    return new Ray(eye, dir, near, far);
  }

  /** The cached value. */
  private Vec4 leftCache;

  /**
   * Getter.
   *
   * @return The direction to the left.
   */
  public Vec4 getLeft() {
    if(leftCache == null) {
      leftCache = up.cross(view).normalized();
    }
    return leftCache;
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

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[fov: " + getFov() + " width: " + getWidth()
        + " height: " + getHeight() + " near: " + getNear() + " far: " + getFar()
        + "\neye: " + getEye() + "\nview: " + getView() + "\nup: " + getUp() + "]";
  }

}
