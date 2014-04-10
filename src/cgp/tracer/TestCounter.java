package cgp.tracer;

/**
 * Counts the number of triangle checks.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TestCounter {

  /** The number of triangle checks. */
  private int count;

  /** Creates an empty counter. */
  public TestCounter() {
    count = 0;
  }

  /** Increases the check count. */
  public void addCheck() {
    ++count;
  }

  /**
   * Adds the check count from another counter.
   * 
   * @param o The other counter.
   */
  public void addChecks(final TestCounter o) {
    count += o.count;
  }

  /**
   * Getter.
   * 
   * @return The number of triangle checks.
   */
  public int getCount() {
    return count;
  }

}
