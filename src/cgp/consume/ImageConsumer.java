package cgp.consume;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import cgp.tracer.Hit;

/**
 * Produces an image with the results.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class ImageConsumer implements HitConsumer {

  /** The image. */
  private BufferedImage img;

  @Override
  public void setSize(final int width, final int height) {
    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }

  /**
   * Computes the color.
   * 
   * @param hit The hit.
   * @return The color.
   */
  protected abstract int getRGB(Hit hit);

  @Override
  public void hitAt(final Hit hit, final int x, final int y) {
    img.setRGB(x, y, 0xff000000 | getRGB(hit));
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

  /**
   * Saves the image if it has been finished.
   * 
   * @param dest The destination file.
   * @return Whether the image was ready.
   * @throws IOException I/O Exception.
   */
  public boolean saveImage(final File dest) throws IOException {
    if(img == null) return false;
    ImageIO.write(img, "PNG", dest);
    return true;
  }

}
