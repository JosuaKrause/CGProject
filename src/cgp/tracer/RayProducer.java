package cgp.tracer;

import cgp.data.Ray;
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

}
