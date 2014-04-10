package cgp.consume;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import cgp.tracer.Hit;

/**
 * Produces an image with the test count.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TestCountConsumer implements HitConsumer {

  /** The image. */
  private BufferedImage img;

  @Override
  public void setSize(final int width, final int height) {
    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }

  @Override
  public void hitAt(final Hit hit, final int x, final int y) {
    final int sub = (int) (hit.getTestCount() * 0xff);
    final int alpha = 0xff;
    final int red = 0xff;
    final int green = 0xff - sub;
    final int blue = 0xff - sub;
    img.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
  }

  /**
   * Draws the image.
   * 
   * @param g The graphics context.
   */
  public void draw(final Graphics2D g) {
    if(img == null) {
      g.setColor(Color.BLACK);
      g.fill(new Rectangle2D.Double(0, 0, 10, 10));
    } else {
      g.drawImage(img, 0, 0, null);
    }
  }

}
