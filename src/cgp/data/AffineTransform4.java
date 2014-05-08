package cgp.data;

/**
 * An affine transformation for a three dimensional space.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AffineTransform4 {

  /** The identity transformation. */
  public static final AffineTransform4 IDENTITY = new AffineTransform4(
      new double[][] {
          { 1, 0, 0, 0},
          { 0, 1, 0, 0},
          { 0, 0, 1, 0},
          { 0, 0, 0, 1}});

  /**
   * Creates a translation transformation.
   * 
   * @param dx The x translation.
   * @param dy The y translation.
   * @param dz The z translation.
   * @return The translation transformation.
   */
  public static final AffineTransform4 translation(
      final double dx, final double dy, final double dz) {
    return new AffineTransform4(
        new double[][] {
            { 1, 0, 0, dx},
            { 0, 1, 0, dy},
            { 0, 0, 1, dz},
            { 0, 0, 0, 1}});
  }

  /**
   * Creates a scaling transformation.
   * 
   * @param sx The x scale.
   * @param sy The y scale.
   * @param sz The z scale.
   * @return The scaling transformation.
   */
  public static final AffineTransform4 scale(
      final double sx, final double sy, final double sz) {
    return new AffineTransform4(
        new double[][] {
            { sx, 0, 0, 0},
            { 0, sy, 0, 0},
            { 0, 0, sz, 0},
            { 0, 0, 0, 1}});
  }

  /**
   * Creates a rotation transformation on the x axis.
   * 
   * @param alpha The angle.
   * @return The rotation transformation.
   */
  public static final AffineTransform4 rotateX(final double alpha) {
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new AffineTransform4(
        new double[][] {
            { 1, 0, 0, 0},
            { 0, cos, sin, 0},
            { 0, -sin, cos, 0},
            { 0, 0, 0, 1}});
  }

  /**
   * Creates a rotation transformation on the y axis.
   * 
   * @param alpha The angle.
   * @return The rotation transformation.
   */
  public static final AffineTransform4 rotateY(final double alpha) {
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new AffineTransform4(
        new double[][] {
            { cos, 0, -sin, 0},
            { 0, 1, 0, 0},
            { sin, 0, cos, 0},
            { 0, 0, 0, 1}});
  }

  /**
   * Creates a rotation transformation on the z axis.
   * 
   * @param alpha The angle.
   * @return The rotation transformation.
   */
  public static final AffineTransform4 rotateZ(final double alpha) {
    final double sin = Math.sin(alpha);
    final double cos = Math.cos(alpha);
    return new AffineTransform4(
        new double[][] {
            { cos, sin, 0, 0},
            { -sin, cos, 0, 0},
            { 0, 0, 1, 0},
            { 0, 0, 0, 1}});
  }

  /**
   * The matrix representing the affine transformation. The order is rows and
   * then columns.
   */
  private final double[][] mat;

  /**
   * Creates an affine transformation.
   * 
   * @param mat The internal matrix.
   */
  private AffineTransform4(final double[][] mat) {
    this.mat = mat;
  }

  /**
   * Concatenates the transformation to the given transformation. Suppose
   * <code>A = this</code> and <code>B = o</code> then the resulting
   * transformation is <code>A * B</code>.
   * 
   * @param o The transformation to concatenate.
   * @return The resulting transformation.
   */
  public AffineTransform4 concatenate(final AffineTransform4 o) {
    final double[][] res = new double[4][4];
    for(int r = 0; r < 4; ++r) {
      final double[] cur = res[r];
      final double[] m = mat[r];
      for(int c = 0; c < 4; ++c) {
        double sum = 0;
        for(int i = 0; i < 4; ++i) {
          sum += m[i] * o.mat[i][c];
        }
        cur[c] = sum;
      }
    }
    return new AffineTransform4(res);
  }

  /**
   * Transforms the given vector according to the affine transformation.
   * 
   * @param vec The vector to transform.
   * @return The transformed vector.
   */
  public Vec4 transform(final Vec4 vec) {
    final double x = vec.getX();
    final double y = vec.getY();
    final double z = vec.getZ();
    final double w = vec.getW();
    final double[] res = new double[4];
    for(int r = 0; r < 4; ++r) {
      final double[] m = mat[r];
      res[r] = x * m[0] + y * m[1] + z * m[2] + w * m[3];
    }
    final double rw = res[3];
    if(rw == 0) return new Vec4(res[0], res[1], res[2], false);
    return new Vec4(res[0] / rw, res[1] / rw, res[2] / rw, true);
  }

  /**
   * Transforms the given triangle according to the affine transformation.
   * 
   * @param tri The triangle to transform.
   * @return The transformed triangle.
   */
  public Triangle transform(final Triangle tri) {
    return new Triangle(
        transform(tri.getA()),
        transform(tri.getB()),
        transform(tri.getC()),
        transform(tri.getANormal()),
        transform(tri.getBNormal()),
        transform(tri.getCNormal()));
  }

}
