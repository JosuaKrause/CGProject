package cgp.tracer;

/**
 * Counts the number of triangle checks.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class TestCounter {

  /** The number of triangle checks. */
  private int count;
  /** The number of bounding box checks. */
  private int bboxCount;

  /** Creates an empty counter. */
  public TestCounter() {
    count = 0;
    bboxCount = 0;
  }

  /** Increases the triangle check count. */
  public void addCheck() {
    ++count;
  }

  public void addBBoxCheck() {
    ++bboxCount;
  }

  /**
   * Adds the check count from another counter.
   *
   * @param o The other counter.
   */
  public void addChecks(final TestCounter o) {
    count += o.count;
    bboxCount += o.bboxCount;
  }

  /**
   * Getter.
   *
   * @return The number of triangle checks.
   */
  public int getCount() {
    return count;
  }

  public int getBBoxCount() {
    return bboxCount;
  }

}
