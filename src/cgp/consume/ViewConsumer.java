package cgp.consume;

import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.data.Vec4;
import cgp.tracer.Hit;

/**
 * Creates the color from the surface direction.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ViewConsumer extends ImageConsumer {

  @Override
  public String name() {
    return "view";
  }

  @Override
  protected int getRGB(final Hit hit) {
    if(!hit.hasHit()) return 0x0;
    final Triangle t = hit.getTriangle();
    final Ray ray = hit.getRay();
    final Vec4 norm = t.getNormalAt(ray.getPosition(hit.getDistance()));
    final double angle = ray.getDirection().negate().angle(norm);
    final int grey = 255 - (int) (angle * 2.0 / Math.PI * 255);
    return grey << 16 | grey << 8 | grey;
  }

}
