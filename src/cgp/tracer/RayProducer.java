package cgp.tracer;

import cgp.data.Ray;

/**
 * Produces a grid of rays.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface RayProducer {

  /**
   * Getter.
   * 
   * @param x The x position.
   * @param y The y position.
   * @return The ray for the given position.
   */
  Ray getFor(int x, int y);

  /**
   * Getter.
   * 
   * @return The width of the grid.
   */
  int getWidth();

  /**
   * Getter.
   * 
   * @return The height of the grid.
   */
  int getHeight();

}
