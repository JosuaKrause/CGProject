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

  /** All values. */
  private long[][] values;
  /** The minimal value. */
  private long min;
  /** The maximal value. */
  private long max;

  @Override
  public void setSize(final int width, final int height) {
    super.setSize(width, height);
    values = new long[width][height];
    min = Long.MAX_VALUE;
    max = Long.MIN_VALUE;
  }

  /**
   * Getter.
   *
   * @param hit The hit.
   * @return The desired hit count between zero and one.
   */
  private long getCount(final Hit hit) {
    return triangles ? hit.getTestCount() : hit.getBBoxCount();
  }

  /**
   * Normalizes the given value.
   *
   * @param v The value.
   * @return The normalized value.
   */
  private double normalize(final double v) {
    return (v - min) / (max - min);
  }

  @Override
  public void hitAt(final Hit hit, final int x, final int y) {
    final long v = getCount(hit);
    values[x][y] = v;
    if(min > v) {
      min = v;
    }
    if(max < v) {
      max = v;
    }
  }

  @Override
  protected int getRGB(final Hit hit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void finished() {
    System.out.println(name() + "[min: " + min + " max: " + max + "]");
    for(int x = 0; x < values.length; ++x) {
      final long[] row = values[x];
      for(int y = 0; y < row.length; ++y) {
        final int sub = (int) (normalize(row[y]) * 0xff);
        final int red = triangles ? 0xff : 0;
        final int green = triangles ? 0xff - sub : sub;
        final int blue = triangles ? 0xff - sub : 0;
        setRGB(red << 16 | green << 8 | blue, x, y);
      }
    }
  }

}
