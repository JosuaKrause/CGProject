package cgp.consume;

import cgp.tracer.Hit;

/**
 * Produces an image with the test count.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class CompareTestCountConsumer extends ImageConsumer {

  /** Whether to show triangle checks. */
  private final boolean triangles;

  /**
   * Creates a test count consumer.
   *
   * @param triangles Whether to show triangle checks.
   */
  public CompareTestCountConsumer(final boolean triangles) {
    this.triangles = triangles;
  }

  @Override
  public String name() {
    return (triangles ? "triangles" : "bboxes") + " diff";
  }

  /** All values. */
  private long[][] values;
  /** The previous values. */
  private long[][] prev;
  /** The maximal value. */
  private long max;

  @Override
  public void setSize(final int width, final int height) {
    super.setSize(width, height);
    max = 0;
    if(values != null && values.length == width
        && (width == 0 || values[0].length == height)) {
      prev = values;
      for(final long[] cols : prev) {
        for(final long cell : cols) {
          if(cell > max) {
            max = cell;
          }
        }
      }
    }
    values = new long[width][height];
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

  @Override
  public void hitAt(final Hit hit, final int x, final int y) {
    final long v = getCount(hit);
    values[x][y] = v;
    if(max < v) {
      max = v;
    }
  }

  @Override
  protected int getRGB(final Hit hit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Normalizes the difference values.
   * 
   * @param fresh The fresh value.
   * @param old The old value.
   * @return The normalized difference.
   */
  private double normalize(final double fresh, final double old) {
    return (fresh - old) / max;
  }

  @Override
  public void finished() {
    System.out.println(name() + "[max: " + max + "]");
    for(int x = 0; x < values.length; ++x) {
      final long[] row = values[x];
      final long[] prow = prev != null ? prev[x] : null;
      for(int y = 0; y < row.length; ++y) {
        final double d = normalize(row[y], prow != null ? prow[y] : 0);
        final int add = d <= 0 ? 0 : (int) (d * 0xff);
        final int sub = d >= 0 ? 0 : (int) (-d * 0xff);
        final int red = add;
        final int green = 0;
        final int blue = sub;
        setRGB(red << 16 | green << 8 | blue, x, y);
      }
    }
  }

}
