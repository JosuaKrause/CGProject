package cgp.algos;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Objects;

import cgp.data.BoundingBox;
import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.data.Vec4;
import cgp.tracer.Hit;
import cgp.tracer.TestCounter;

/**
 * An Octree.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class Octree extends SimpleStorage {

  /**
   * An internal node of the Octree.
   *
   * @author Joschi <josua.krause@gmail.com>
   */
  private final class Node {

    /** The bounding box. */
    private final BoundingBox box;
    /** The set of triangles or <code>null</code> if inner node. */
    private BitSet ts;
    /** The offset of the bit set. */
    private int offset;
    /** The children or <code>null</code> if leaf. */
    private Node[] children;

    /**
     * Creates a new node.
     *
     * @param box The bounding box.
     */
    public Node(final BoundingBox box) {
      this.box = Objects.requireNonNull(box);
      ts = new BitSet();
      children = null;
      offset = 0;
    }

    /**
     * Adds a triangle.
     *
     * @param index The index.
     * @param t The triangle.
     */
    public void addTriangle(final int index, final Triangle t) {
      addTriangle(index, t, false);
    }

    /**
     * Adds a triangle.
     *
     * @param index The index.
     * @param t The triangle.
     * @param noSplit Whether splits are allowed.
     */
    private void addTriangle(final int index, final Triangle t, final boolean noSplit) {
      if(!box.intersects(t)) return;
      if(ts != null) {
        if(ts.get(index - offset)) return;
        addTriangle(index, noSplit);
        return;
      }
      for(final Node n : children) {
        n.addTriangle(index, t, noSplit);
      }
    }

    /**
     * Actually adds the triangle.
     *
     * @param index The index.
     * @param noSplit Whether splits are allowed.
     */
    private void addTriangle(final int index, final boolean noSplit) {
      ts.set(index - offset);
      if(noSplit) return;
      if(ts.cardinality() > threshold) {
        splitNode();
      }
    }

    /** Splits the node. */
    private void splitNode() {
      final BoundingBox[] boxes = new BoundingBox[8];
      children = new Node[8];
      final BitSet b = ts;
      ts = null;
      split(box, boxes);
      for(int i = 0; i < children.length; ++i) {
        final Node n = new Node(boxes[i]);
        for(int t = b.nextSetBit(0); t >= 0; t = b.nextSetBit(t + 1)) {
          n.addTriangle(t + offset, getTriangle(t + offset), true);
        }
        children[i] = n;
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
      if(ts != null) return getLevelHit(r, c);
      final Vec4 d = r.getDirection();
      final boolean[] mins = {
          d.getX() > 0,
          d.getY() > 0,
          d.getZ() > 0
      };
      final double x = Math.abs(d.getX());
      final double y = Math.abs(d.getY());
      final double z = Math.abs(d.getZ());
      final int first = x < y ? (x < z ? 0 : 2) : (y < z ? 1 : 2);
      final int last = x > y ? (x > z ? 0 : 2) : (y > z ? 1 : 2);
      final int mid = 3 - first - last;
      for(int round = 0; round < 8; ++round) {
        final Node n = children[index(mins[0], mins[1], mins[2])];
        final Hit hit = n.getHit(r, c);
        if(hit.hasHit()) return hit;
        mins[first] = !mins[first];
        if((round & 1) == 1) {
          mins[mid] = !mins[mid];
        }
        if((round & 3) == 3) {
          mins[last] = !mins[last];
        }
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
      for(int t = ts.nextSetBit(0); t >= 0; t = ts.nextSetBit(t + 1)) {
        final Triangle tri = getTriangle(t + offset);
        final double dist = tri.hit(r, c);
        if(r.isValidDistance(dist) && dist < minDist) {
          minDist = dist;
          curBest = tri;
        }
      }
      return new Hit(r, curBest, minDist, c);
    }

    /** Optimizes the bitset storage by shifting it to its lowest set bit. */
    public void optimize() {
      if(ts == null) {
        for(final Node c : children) {
          c.optimize();
        }
        return;
      }
      final int lowestIndex = ts.nextSetBit(0);
      if(lowestIndex <= 0) return;
      final BitSet set = new BitSet(ts.length() - lowestIndex);
      for(int t = ts.nextSetBit(0); t >= 0; t = ts.nextSetBit(t + 1)) {
        set.set(t - lowestIndex);
      }
      ts = set;
      offset += lowestIndex;
    }

  } // Node

  /** The threshold when boxes are not being split anymore. */
  protected final int threshold;
  /** The bounding box of the scene. */
  private BoundingBox bbox = new BoundingBox();
  /** The root node. */
  private Node root;

  /**
   * Creates an octree.
   *
   * @param threshold The threshold.
   */
  public Octree(final int threshold) {
    if(threshold < 1) throw new IllegalArgumentException("" + threshold);
    this.threshold = threshold;
    root = null;
  }

  @Override
  public void addTriangle(final Triangle tri) {
    super.addTriangle(tri);
    bbox = bbox.add(new BoundingBox(tri));
  }

  @Override
  public void finishLoading() {
    sort(new Comparator<Triangle>() {

      @Override
      public int compare(final Triangle o1, final Triangle o2) {
        final Vec4 m1 = o1.getMin();
        final Vec4 m2 = o2.getMin();
        final int x = Double.compare(m1.getX(), m2.getX());
        if(x != 0) return x;
        final int y = Double.compare(m1.getY(), m2.getY());
        if(y != 0) return y;
        return Double.compare(m1.getZ(), m2.getZ());
      }

    });
    root = new Node(bbox);
    for(int i = 0; i < size(); ++i) {
      root.addTriangle(i, getTriangle(i));
    }
    root.optimize();
  }

  /**
   * Splits the bounding box.
   *
   * @param box The bounding box.
   * @param dest An array with length 8 that has the result after the call.
   */
  protected static final void split(final BoundingBox box, final BoundingBox[] dest) {
    final Vec4 center = box.getCenter();
    boolean minX = false;
    boolean minY = false;
    boolean minZ = false;
    for(;;) {
      final Vec4 vec = box.get(minX, minY, minZ);
      dest[index(minX, minY, minZ)] = new BoundingBox(vec, center);
      minX = !minX;
      if(!minX) {
        minY = !minY;
        if(!minY) {
          minZ = !minZ;
          if(!minZ) {
            break;
          }
        }
      }
    }
  }

  /**
   * Getter.
   *
   * @param minX Minimal x.
   * @param minY Minimal y.
   * @param minZ Minimal z.
   * @return Computes the index of the specified node.
   */
  protected static final int index(
      final boolean minX, final boolean minY, final boolean minZ) {
    return (minX ? 1 : 0) | (minY ? 2 : 0) | (minZ ? 4 : 0);
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    return root.getHit(r, c);
  }

}
