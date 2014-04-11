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
public class NormalConsumer extends ImageConsumer {

  @Override
  public String name() {
    return "normal";
  }

  @Override
  protected int getRGB(final Hit hit) {
    if(!hit.hasHit()) return 0x0;
    final Triangle t = hit.getTriangle();
    final Ray ray = hit.getRay();
    final Vec4 norm = t.getNormalAt(ray.getPosition(hit.getDistance()));
    final int red = (int) (0xff * (norm.getX() * 0.5 + 0.5));
    final int green = (int) (0xff * (norm.getY() * 0.5 + 0.5));
    final int blue = (int) (0xff * (norm.getZ() * 0.5 + 0.5));
    if(red > 255 || red < 0
        || green > 255 || green < 0
        || blue > 255 || blue < 0) throw new IllegalStateException(
        red + " " + green + " " + blue);
    return red << 16 | green << 8 | blue;
  }

}
