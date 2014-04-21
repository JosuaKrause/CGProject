package cgp.consume;

import cgp.tracer.Hit;

/**
 * Produces an image with the test count.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class TestCountConsumer extends ImageConsumer {

  /** Whether to show triangle checks. */
  private final boolean triangles;

  /**
   * Creates a test count consumer.
   *
   * @param triangles Whether to show triangle checks.
   */
  public TestCountConsumer(final boolean triangles) {
    this.triangles = triangles;
  }

  @Override
  public String name() {
    return (triangles ? "triangles" : "bboxes") + " checks";
  }

  @Override
  protected int getRGB(final Hit hit) {
    final int sub = (int) ((triangles ? hit.getTestCount() : hit.getBBoxCount()) * 0xff);
    final int red = triangles ? 0xff : 0;
    final int green = triangles ? 0xff - sub : sub;
    final int blue = triangles ? 0xff - sub : 0;
    return red << 16 | green << 8 | blue;
  }

}
