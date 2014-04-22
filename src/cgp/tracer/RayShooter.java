package cgp.tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import cgp.algos.TriangleStorage;
import cgp.consume.HitConsumer;
import cgp.data.Ray;

/**
 * The ray shooter.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class RayShooter {

  /** All hit consumers. */
  private final List<HitConsumer> consumers = new ArrayList<>();
  /** The ray producer. */
  private final RayProducer prod;
  /** The triangle storage. */
  private final TriangleStorage storage;

  /**
   * Creates a new ray shooter.
   *
   * @param prod The ray producer.
   * @param storage The triangle storage.
   */
  public RayShooter(final RayProducer prod, final TriangleStorage storage) {
    this.storage = Objects.requireNonNull(storage);
    this.prod = Objects.requireNonNull(prod);
    System.out.println("using " + fjp.getParallelism() + " cores");
  }

  /**
   * Adds a consumer.
   *
   * @param hc A hit consumer.
   */
  public void addConsumer(final HitConsumer hc) {
    consumers.add(Objects.requireNonNull(hc));
  }

  /** The internal fork join pool. */
  private final ForkJoinPool fjp = new ForkJoinPool();

  /**
   * A ray shooting action. The action is divided until the number of rays to
   * shoot is under a given threshold.
   *
   * @author Joschi <josua.krause@gmail.com>
   */
  private class ShootingAction extends RecursiveAction {

    /** The hit array. */
    private final Hit[][] hits;
    /** The personal test counter. */
    private final TestCounter counter;
    /** The lowest inclusive x coordinate. */
    private final int xFrom;
    /** The highest exclusive x coordinate. */
    private final int xTo;
    /** The lowest inclusive y coordinate. */
    private final int yFrom;
    /** The highest exclusive y coordinate. */
    private final int yTo;

    /**
     * Creates a shooting action.
     *
     * @param hits The hit array.
     * @param xFrom The lowest inclusive x coordinate.
     * @param xTo The highest exclusive x coordinate.
     * @param yFrom The lowest inclusive y coordinate.
     * @param yTo The highest exclusive y coordinate.
     */
    public ShootingAction(final Hit[][] hits,
        final int xFrom, final int xTo, final int yFrom, final int yTo) {
      this.hits = hits;
      this.xFrom = xFrom;
      this.xTo = xTo;
      this.yFrom = yFrom;
      this.yTo = yTo;
      counter = new TestCounter();
    }

    /** Actually computes the rays. */
    private void doCompute() {
      for(int x = xFrom; x < xTo; ++x) {
        final Hit[] col = hits[x];
        for(int y = yFrom; y < yTo; ++y) {
          shootRay(col, x, y, counter);
        }
      }
    }

    /** The ray threshold. */
    private static final int THRESHOLD = 4000;

    /**
     * Getter.
     *
     * @return Whether the number of rays is below the threshold.
     */
    private boolean isSmallTask() {
      return (xTo - xFrom) * (yTo - yFrom) <= THRESHOLD;
    }

    @Override
    protected void compute() {
      if(isSmallTask()) {
        doCompute();
        return;
      }
      final ShootingAction sa;
      final ShootingAction sb;
      if(xTo - xFrom > 1) {
        final int xMid = xFrom + (xTo - xFrom) / 2;
        sa = new ShootingAction(hits, xFrom, xMid, yFrom, yTo);
        sb = new ShootingAction(hits, xMid, xTo, yFrom, yTo);
      } else {
        final int yMid = yFrom + (yTo - yFrom) / 2;
        sa = new ShootingAction(hits, xFrom, xTo, yFrom, yMid);
        sb = new ShootingAction(hits, xFrom, xTo, yMid, yTo);
      }
      sa.fork();
      sb.fork();
      sa.join();
      sb.join();
      counter.addChecks(sa.counter);
      counter.addChecks(sb.counter);
    }

    /**
     * Getter.
     *
     * @return The total number of triangle tests.
     */
    public long getTotalTestCount() {
      return counter.getCount();
    }

  } // ShootingAction

  /**
   * Shoots all rays. The consumers get notified after the shooting is
   * completed.
   *
   * @return The number of triangle checks.
   */
  public long shootRays() {
    final int w = prod.getWidth();
    final int h = prod.getHeight();
    final Hit[][] res = new Hit[w][h];
    final ShootingAction sa = new ShootingAction(res, 0, w, 0, h);
    fjp.invoke(sa);
    finish(res);
    return sa.getTotalTestCount();
  }

  /**
   * Shoots a single ray.
   *
   * @param hits A column of the hit array.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param all The total triangle check counter.
   */
  void shootRay(final Hit[] hits, final int x, final int y, final TestCounter all) {
    final Ray r = prod.getFor(x, y);
    final TestCounter c = new TestCounter();
    final Hit h = storage.getHit(r, c);
    all.addChecks(c);
    hits[y] = h;
  }

  /**
   * Notifies the consumers of the result.
   *
   * @param hits The filled hit array.
   */
  private void finish(final Hit[][] hits) {
    for(final HitConsumer hc : consumers) {
      for(int x = 0; x < hits.length; ++x) {
        final Hit[] col = hits[x];
        if(x == 0) {
          hc.setSize(hits.length, col.length);
        }
        for(int y = 0; y < col.length; ++y) {
          hc.hitAt(col[y], x, y);
        }
      }
      hc.finished();
    }
  }

}
