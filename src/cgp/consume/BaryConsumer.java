package cgp.consume;

import cgp.data.BarycentricCoordinates;
import cgp.tracer.Hit;

/**
 * Shows the barycentric coordinates as rgb values.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class BaryConsumer extends ImageConsumer {

  @Override
  public String name() {
    return "bary";
  }

  @Override
  protected int getRGB(final Hit hit) {
    if(!hit.hasHit()) return 0;
    final BarycentricCoordinates b = hit.getBarycentric();
    final int red = (int) (0xff * b.getU());
    final int green = (int) (0xff * b.getV());
    final int blue = (int) (0xff * b.getW());
    return red << 16 | green << 8 | blue;
  }

}
