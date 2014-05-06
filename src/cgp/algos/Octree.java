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
public class Octree extends Hitter {

  /**
   * An internal node of the Octree.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private final class Node {

    /** The bounding box. */
    private final BoundingBox box;
    /** The depth of the node. */
    private final int depth;
    /** The set of triangles or <code>null</code> if inner node. */
    private BitSet tset;
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
     * @param depth The depth of the node.
     */
    public Node(final BoundingBox box, final int depth) {
      this.box = Objects.requireNonNull(box);
      this.depth = depth;
      tset = new BitSet();
      children = null;
      offset = 0;
      maximumDepth = Math.max(depth, maximumDepth);
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
      if(tset != null) {
        tset.set(index - offset);
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
      if(depth >= depthThreshold) return;
      if(tset.isEmpty() || tset.cardinality() <= triangleThreshold || noSplit) return;
      if(box.getWidth() <= minDist || box.getHeight() <= minDist
          || box.getDepth() <= minDist) return;
      final BoundingBox[] boxes = new BoundingBox[8];
      children = new Node[8];
      final BitSet b = tset;
      tset = null;
      final Vec4 mid = split(box, boxes);
      boolean unsplit = true;
      for(int i = 0; i < children.length; ++i) {
        final Node n = new Node(boxes[i], depth + 1);
        for(int t = b.nextSetBit(0); t >= 0; t = b.nextSetBit(t + 1)) {
          final int index = t + offset;
          unsplit = n.addTriangle(index, ts.getTriangle(index), i, mid) && unsplit;
        }
        children[i] = n;
        n.optimize();
      }
      if(unsplit) {
        tset = b;
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
      if(tset != null) return getLevelHit(r, c);
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
      for(int t = tset.nextSetBit(0); t >= 0; t = tset.nextSetBit(t + 1)) {
        final Triangle tri = ts.getTriangle(t + offset);
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
      if(tset == null) {
        for(final Node c : children) {
          c.optimize();
        }
        return;
      }
      final int lowestIndex = tset.nextSetBit(0);
      if(lowestIndex <= 0) return;
      final BitSet set = new BitSet(tset.length() - lowestIndex);
      for(int t = tset.nextSetBit(0); t >= 0; t = tset.nextSetBit(t + 1)) {
        set.set(t - lowestIndex);
      }
      tset = set;
      offset += lowestIndex;
    }

    /**
     * Counts the number of bounding boxes in the octree.
     */
    public void countBoundingBoxes() {
      ++totalBoundingBoxes;
      if(ts != null) {
        if(children == null) return;
        for(final Node n : children) {
          if(n != null) {
            n.countBoundingBoxes();
          }
        }
      }
    }
  } // Node

  /**
   * The threshold when boxes are not being split anymore because of the number
   * of triangles in the node.
   */
  protected final int triangleThreshold;
  /**
   * The threshold when boxes are not being split anymore because of the depth
   * of the tree.
   */
  protected final int depthThreshold;
  /** The bounding box of the scene. */
  private BoundingBox bbox;
  /** The root node. */
  private Node root;
  /** The minimal distance between triangle end-points. */
  protected double minDist;
  /**
   * The maximum depth of the octree.
   */
  protected int maximumDepth;
  /**
   * The number of bounding boxes in the octree.
   */
  protected int totalBoundingBoxes;

  /**
   * Creates an Octree.
   * 
   * @param depthThreshold The depth threshold.
   * @param triangleThreshold The triangle threshold.
   */
  public Octree(final int depthThreshold, final int triangleThreshold) {
    if(triangleThreshold < 1) throw new IllegalArgumentException("" + triangleThreshold);
    if(depthThreshold < 1) throw new IllegalArgumentException("" + depthThreshold);
    this.depthThreshold = depthThreshold;
    this.triangleThreshold = triangleThreshold;

  }

  @Override
  protected void build() {
    maximumDepth = 0;
    totalBoundingBoxes = 0;
    root = null;
    minDist = Double.POSITIVE_INFINITY;
    bbox = new BoundingBox();
    for(final Triangle t : ts.getList()) {
      final BoundingBox cur = new BoundingBox(t);
      minDist = Math.min(
          Math.max(Math.max(cur.getWidth(), cur.getHeight()),
              Math.max(cur.getDepth(), 1e-9)), minDist);
      bbox = bbox.add(cur);
    }
    root = new Node(bbox, 0);
    for(int i = 0; i < ts.size(); ++i) {
      root.addTriangle(i, ts.getTriangle(i));
    }
    root.splitNode();
    root.optimize();
    root.countBoundingBoxes();
    System.out.println("Depth of octree: " + maximumDepth);
    System.out.println("Bounding boxes in octree: " + totalBoundingBoxes);
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
