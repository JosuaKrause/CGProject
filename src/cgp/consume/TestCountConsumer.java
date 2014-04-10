package cgp.consume;

import cgp.tracer.Hit;

/**
 * Produces an image with the test count.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TestCountConsumer extends ImageConsumer {

  @Override
  protected int getRGB(final Hit hit) {
    final int sub = (int) (hit.getTestCount() * 0xff);
    final int red = 0xff;
    final int green = 0xff - sub;
    final int blue = 0xff - sub;
    return red << 16 | green << 8 | blue;
  }

}
