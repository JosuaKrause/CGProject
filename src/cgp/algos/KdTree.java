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
  private final class KdNode implements Comparator<Triangle> {
    // 0 Near, 1 Far
    KdNode children[];
    // 0 X-axis split, 1 Y-axis split, 2 Z-axis split
    int splitType;
    // Split value along axis
    int splitIndex;
    double splitValue;
    // Bounding box
    private final BoundingBox box;
    // triangles contained within node. Value is null if node is not a leaf
    private List<Triangle> tri;

    // Triangles need to be sorted
    @Override
    public int compare(final Triangle t1, final Triangle t2) {
      double comparison;
      if(splitType == 0) {
        comparison = Math.min(t1.getA().getX(),
            Math.min(t1.getB().getX(), t1.getC().getX()))
            - Math.min(t2.getA().getX(), Math.min(t2.getB().getX(), t2.getC().getX()));
      }
      else if(splitType == 1) {
        comparison = Math.min(t1.getA().getY(),
            Math.min(t1.getB().getY(), t1.getC().getY()))
            - Math.min(t2.getA().getY(), Math.min(t2.getB().getY(), t2.getC().getY()));
      }
      else {
        comparison = Math.min(t1.getA().getZ(),
            Math.min(t1.getB().getZ(), t1.getC().getZ()))
            - Math.min(t2.getA().getZ(), Math.min(t2.getB().getZ(), t2.getC().getZ()));
      }
      if(comparison < 0) return -1;
      else if(comparison > 0) return 1;
      else return 0;
    }

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
      Collections.sort(triangles, this);
      splitIndex = (triangles.size() - 1) / 2;
      switch(splitType) {
        case 0:
          splitValue = Math.min(triangles.get(splitIndex).getA().getX(), Math.min(
              triangles.get(splitIndex).getB().getX(),
              triangles.get(splitIndex).getC().getX()));
          break;
        case 1:
          splitValue = Math.min(triangles.get(splitIndex).getA().getY(), Math.min(
              triangles.get(splitIndex).getB().getY(),
              triangles.get(splitIndex).getC().getY()));
          break;
        case 2:
          splitValue = Math.min(triangles.get(splitIndex).getA().getZ(), Math.min(
              triangles.get(splitIndex).getB().getZ(),
              triangles.get(splitIndex).getC().getZ()));
          break;
      }
      final List<Triangle> leftBottomNear = new ArrayList<>(triangles.subList(0,
          splitIndex));
      final List<Triangle> rightTopFar = new ArrayList<>(triangles.subList(
          splitIndex, triangles.size()));
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
          max1 = new Vec4(splitValue, max.getY(), splitValue, max.isPoint());
          min2 = new Vec4(splitValue, min.getY(), splitValue, min.isPoint());
          break;
      }
      BoundingBox b = new BoundingBox(min, max1);
      children[0] = leftBottomNear.size() == 0 ? null : new KdNode(b,
          (splitType + 1) % 3);
      b = new BoundingBox(min2, max);
      children[1] = rightTopFar.size() == 0 ? null : new KdNode(b, (splitType + 1) % 3);
      if(leftBottomNear.size() == triangles.size()
          || rightTopFar.size() == triangles.size()) {
        tri = triangles;
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
    root.buildKdTree(triangles);
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    return root.getHit(r, c);
  }
}
