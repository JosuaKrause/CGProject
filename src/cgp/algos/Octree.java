package cgp.algos;

import java.util.BitSet;
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
    /** Indicates that this node does not need to be split. */
    private boolean noSplit;

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
      addTriangle(index, t, 0, null);
    }

    /**
     * Adds a triangle.
     *
     * @param index The triangle index.
     * @param t The triangle.
     * @param boxIndex The index of the bounding box in the parent.
     * @param mid The middle point of the parent bounding box.
     * @return Whether the triangle was added.
     */
    private boolean addTriangle(final int index, final Triangle t, final int boxIndex,
        final Vec4 mid) {
      if(mid != null) {
        for(int axis = 0; axis < 3; ++axis) {
          final int rel = t.relToPlane(mid.get(axis), axis);
          if(isMinNode(boxIndex, axis) ? rel > 0 : rel < 0) return false;
        }
      }
      if(ts != null) {
        ts.set(index - offset);
        noSplit = false;
        return true;
      }
      boolean wasAdded = false;
      final Vec4 center = box.getCenter();
      for(int i = 0; i < children.length; ++i) {
        wasAdded = children[i].addTriangle(index, t, i, center) || wasAdded;
      }
      return wasAdded;
    }

    /** Splits the node. */
    public void splitNode() {
      if(ts.isEmpty() || ts.cardinality() <= threshold || noSplit) return;
      if(box.getWidth() <= minDist || box.getHeight() <= minDist
          || box.getDepth() <= minDist) return;
      final BoundingBox[] boxes = new BoundingBox[8];
      children = new Node[8];
      final BitSet b = ts;
      ts = null;
      final Vec4 mid = split(box, boxes);
      boolean unsplit = true;
      for(int i = 0; i < children.length; ++i) {
        final Node n = new Node(boxes[i]);
        for(int t = b.nextSetBit(0); t >= 0; t = b.nextSetBit(t + 1)) {
          final int index = t + offset;
          unsplit = n.addTriangle(index, getTriangle(index), i, mid) && unsplit;
        }
        children[i] = n;
        n.optimize();
      }
      if(unsplit) {
        ts = b;
        children = null;
        noSplit = true;
        optimize();
      } else {
        for(final Node n : children) {
          n.splitNode();
        }
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
  /** The minimal distance between triangle end-points. */
  protected double minDist;

  /**
   * Creates an octree.
   *
   * @param threshold The threshold.
   */
  public Octree(final int threshold) {
    if(threshold < 1) throw new IllegalArgumentException("" + threshold);
    this.threshold = threshold;
    root = null;
    minDist = Double.POSITIVE_INFINITY;
  }

  @Override
  public void addTriangle(final Triangle tri) {
    super.addTriangle(tri);
    final BoundingBox cur = new BoundingBox(tri);
    minDist = Math.min(
        Math.max(Math.max(cur.getWidth(), cur.getHeight()),
            Math.max(cur.getDepth(), 1e-9)), minDist);
    bbox = bbox.add(cur);
  }

  @Override
  public void finishLoading() {
    root = new Node(bbox);
    for(int i = 0; i < size(); ++i) {
      root.addTriangle(i, getTriangle(i));
    }
    root.splitNode();
    root.optimize();
  }

  /**
   * Splits the bounding box.
   *
   * @param box The bounding box.
   * @param dest An array with length 8 that has the result after the call.
   * @return The center of the original bounding box.
   */
  protected static final Vec4 split(final BoundingBox box, final BoundingBox[] dest) {
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
    return center;
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

  /**
   * Whether this node has the lower coordinates.
   *
   * @param index The index of the node.
   * @param axis The axis.
   * @return Whether this node has the lower coordinates.
   */
  protected static final boolean isMinNode(final int index, final int axis) {
    return (index & (1 << axis)) != 0;
  }

  @Override
  public Hit getHit(final Ray r, final TestCounter c) {
    return root.getHit(r, c);
  }

}
