package cgp.algos;

import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * A simple triangle storage strategy. Every triangle gets tested for every ray.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleStorage extends Hitter {

  @Override
  protected void build() {
    // nothing to do
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    double minDist = Double.POSITIVE_INFINITY;
    Triangle curBest = null;
    for(final Triangle t : ts.getList()) {
      final double dist = t.hit(r, c);
      if(r.isValidDistance(dist) && dist < minDist) {
        minDist = dist;
        curBest = t;
      }
    }
    return new Hit(r, curBest, minDist, c);
  }

}
