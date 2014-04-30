package cgp.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cgp.data.BoundingBox;
import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.data.Vec4;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * @author Timothy Chu
 */
public class KdTree extends SimpleStorage {
  private final class KdNode {
    // 0 Near, 1 Far
    private final KdNode children[];
    // 0 X-axis split, 1 Y-axis split, 2 Z-axis split
    private final int splitType;
    // Split value along axis
    private int splitIndex;
    private double splitValue;
    // Bounding box
    private final BoundingBox box;
    // triangles contained within node. Value is null if node is not a leaf
    private List<Triangle> tri;

    public KdNode(final BoundingBox box, final int splitType) {
      this.box = Objects.requireNonNull(box);
      children = new KdNode[2];
      this.splitType = splitType;
      tri = null;
    }

    public void buildKdTree(final List<Triangle> triangles) {
      // Sort the list appropriately
      if(triangles.size() <= 1) {
        tri = triangles;
        return;
      }
      final List<Triangle> ts = new ArrayList<>(triangles);
      final int splitType = this.splitType;
      Collections.sort(ts, new Comparator<Triangle>() {

        // Triangles need to be sorted
        @Override
        public int compare(final Triangle t1, final Triangle t2) {
          final Vec4 min1 = t1.getMin();
          final Vec4 min2 = t2.getMin();
          return Double.compare(min1.get(splitType), min2.get(splitType));
        }

      });
      splitIndex = (ts.size() - 1) / 2;
      switch(splitType) {
        case 0:
          splitValue = Math.min(ts.get(splitIndex).getA().getX(), Math.min(
              ts.get(splitIndex).getB().getX(),
              ts.get(splitIndex).getC().getX()));
          break;
        case 1:
          splitValue = Math.min(ts.get(splitIndex).getA().getY(), Math.min(
              ts.get(splitIndex).getB().getY(),
              ts.get(splitIndex).getC().getY()));
          break;
        case 2:
          splitValue = Math.min(ts.get(splitIndex).getA().getZ(), Math.min(
              ts.get(splitIndex).getB().getZ(),
              ts.get(splitIndex).getC().getZ()));
          break;
      }
      final List<Triangle> leftBottomNear = new ArrayList<>(ts.subList(0,
          splitIndex));
      final List<Triangle> rightTopFar = new ArrayList<>(ts.subList(
          splitIndex, ts.size()));
      // Add any triangles that intersect the split line to both lists
      for(final Triangle t : leftBottomNear) {
        switch(splitType) {
          case 0:
            if(Math.max(t.getA().getX(), Math.max(t.getB().getX(),
                t.getC().getX())) > splitValue) {
              rightTopFar.add(t);
            }
            break;
          case 1:
            if(Math.max(t.getA().getY(), Math.max(t.getB().getY(),
                t.getC().getY())) > splitValue) {
              rightTopFar.add(t);
            }
            break;
          case 2:
            if(Math.max(t.getA().getZ(), Math.max(t.getB().getZ(),
                t.getC().getZ())) > splitValue) {
              rightTopFar.add(t);
            }
            break;
        }
      }
      final Vec4 min = box.get(true, true, true);
      final Vec4 max = box.get(false, false, false);
      Vec4 max1 = null;
      Vec4 min2 = null;
      switch(splitType) {
        case 0:
          max1 = new Vec4(splitValue, max.getY(), max.getZ(), max.isPoint());
          min2 = new Vec4(splitValue, min.getY(), min.getZ(), min.isPoint());
          break;
        case 1:
          max1 = new Vec4(max.getX(), splitValue, max.getZ(), max.isPoint());
          min2 = new Vec4(min.getX(), splitValue, min.getZ(), min.isPoint());
          break;
        case 2:
          max1 = new Vec4(max.getX(), max.getY(), splitValue, max.isPoint());
          min2 = new Vec4(min.getX(), min.getY(), splitValue, min.isPoint());
          break;
      }
      final BoundingBox b1 = new BoundingBox(min, max1);
      children[0] = leftBottomNear.isEmpty() ? null : new KdNode(b1, (splitType + 1) % 3);
      final BoundingBox b2 = new BoundingBox(min2, max);
      children[1] = rightTopFar.isEmpty() ? null : new KdNode(b2, (splitType + 1) % 3);
      if(leftBottomNear.size() == ts.size()
          || rightTopFar.size() == ts.size()) {
        tri = ts;
        children[0] = null;
        children[1] = null;
        return;
      }
      if(children[0] != null) {
        children[0].buildKdTree(leftBottomNear);
      }
      if(children[1] != null) {
        children[1].buildKdTree(rightTopFar);
      }
    }

    /**
     * Tests for a hit.
     *
     * @param r The ray.
     * @param c The test counter.
     * @return The hit.
     */
    public Hit getHit(final Ray r, final TestCounter c) {
      if(!box.intersects(r, c)) return new Hit(r, c);
      if(tri != null) return getLevelHit(r, c);
      // TODO you have to determine the order of the children here! it is
      // dependent of the view direction
      for(final KdNode n : children) {
        if(n == null) {
          continue;
        }
        final Hit hit = n.getHit(r, c);
        if(hit.hasHit()) return hit;
      }
      return new Hit(r, c);
    }

    /**
     * Checks for a hit in a leaf node.
     *
     * @param r The ray.
     * @param c The test counter.
     * @return The hit.
     */
    private Hit getLevelHit(final Ray r, final TestCounter c) {
      double minDist = Double.POSITIVE_INFINITY;
      Triangle curBest = null;
      for(final Triangle t : tri) {
        final double dist = t.hit(r, c);
        if(r.isValidDistance(dist) && dist < minDist) {
          minDist = dist;
          curBest = t;
        }
      }
      return new Hit(r, curBest, minDist, c);
    }

  } // Node

  private BoundingBox bbox = new BoundingBox();

  private KdNode root;

  public KdTree() {
    root = null;
  }

  @Override
  public void addTriangle(final Triangle tri) {
    super.addTriangle(tri);
    bbox = bbox.add(new BoundingBox(tri));
  }

  @Override
  public void finishLoading() {
    final int splitType = 0;
    root = new KdNode(bbox, splitType);
    root.buildKdTree(getList());
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    return root.getHit(r, c);
  }

}
