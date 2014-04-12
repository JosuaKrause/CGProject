package cgp.consume;

import cgp.ogl.Camera;
import cgp.tracer.Hit;

/**
 * Displays the distance to the eye.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DepthConsumer extends ImageConsumer {

  /** The camera. */
  private final Camera cam;

  /**
   * Creates a depth consumer.
   * 
   * @param cam The camera.
   */
  public DepthConsumer(final Camera cam) {
    this.cam = cam;
  }

  @Override
  protected int getRGB(final Hit hit) {
    if(!hit.hasHit()) return 0;
    final double dist = hit.getDistance();
    final double d = (dist - cam.getNear()) / (cam.getFar() - cam.getNear());
    final int grey = 255 - (int) (255 * Math.min(Math.max(d, 0), 1));
    return grey << 16 | grey << 8 | grey;
  }

  @Override
  public String name() {
    return "depth";
  }

}
