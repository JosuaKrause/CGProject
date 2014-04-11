package cgp.consume;

import cgp.tracer.Hit;

/**
 * Displays the distance to the eye.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DepthConsumer extends ImageConsumer {

  /** The maximal depth. */
  private final double maxDepth;
  /** The minimal depth. */
  private final double minDepth;

  /**
   * Creates a depth consumer.
   * 
   * @param minDepth The minimal depth.
   * @param maxDepth The maximal depth.
   */
  public DepthConsumer(final double minDepth, final double maxDepth) {
    this.minDepth = minDepth;
    this.maxDepth = maxDepth;
  }

  @Override
  protected int getRGB(final Hit hit) {
    if(!hit.hasHit()) return 0;
    final double dist = hit.getDistance();
    final double d = (dist - minDepth) / (maxDepth - minDepth);
    final int grey = 255 - (int) (255 * Math.min(Math.max(d, 0), 1));
    return grey << 16 | grey << 8 | grey;
  }

  @Override
  public String name() {
    return "depth";
  }

}
