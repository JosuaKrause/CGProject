package cgp.consume;

import cgp.tracer.Hit;

/**
 * Consumes hits.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface HitConsumer {

  /**
   * Sets the size of the consumer.
   *
   * @param width The width.
   * @param height The height.
   */
  void setSize(int width, int height);

  /**
   * Sets a hit at the given position in the grid.
   *
   * @param hit The hit.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  void hitAt(Hit hit, int x, int y);

  /** Is called after all hits have been consumed. */
  void finished();

}
