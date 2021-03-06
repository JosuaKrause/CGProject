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
   * Getter.
   *
   * @return The UI name of this consumer.
   */
  public abstract String name();

  /**
   * Computes the color.
   *
   * @param hit The hit.
   * @return The color.
   */
  protected abstract int getRGB(Hit hit);

  @Override
  public void hitAt(final Hit hit, final int x, final int y) {
    setRGB(getRGB(hit), x, y);
  }

  /**
   * Sets a pixel.
   * 
   * @param rgb The color.
   * @param x The x position.
   * @param y The y position.
   */
  protected void setRGB(final int rgb, final int x, final int y) {
    img.setRGB(x, y, 0xff000000 | rgb);
  }

  /**
   * Draws the image.
   *
   * @param g The graphics context.
   */
  public void draw(final Graphics2D g) {
    if(img == null) {
      g.setColor(Color.BLACK);
      g.fill(new Rectangle2D.Double(10, 10, 10, 10));
      final int size = g.getFont().getSize();
      g.drawString("R: render", 25, 25 + size);
      g.drawString("I: change image", 25, 25 + size * 2);
      g.drawString("P: take photo", 25, 25 + size * 3);
      g.drawString("C: camera coordinates", 25, 25 + size * 4);
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

  @Override
  public void finished() {
    // nothing to do
  }

}
