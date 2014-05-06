package cgp.tracer;

import cgp.data.Ray;
import cgp.data.Vec4;
import cgp.ogl.Camera;

/**
 * Produces a grid of rays.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface RayProducer extends Camera {

  /**
   * Getter.
   *
   * @param x The x position.
   * @param y The y position.
   * @return The ray for the given position.
   */
  Ray getFor(int x, int y);

  /**
   * Setter.
   * 
   * @param eye The camera origin.
   * @param view The viewing direction.
   * @param up The direction which is up for the camera.
   */
  void setView(Vec4 eye, Vec4 view, Vec4 up);

}
