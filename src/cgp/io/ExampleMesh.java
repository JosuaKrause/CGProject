package cgp.io;

import cgp.algos.TriangleStorage;
import cgp.data.AffineTransform4;
import cgp.data.Triangle;
import cgp.data.Vec4;

/**
 * Loads an example mesh.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class ExampleMesh implements MeshLoader {

  @Override
  public void loadMesh(final TriangleStorage ts, final AffineTransform4 aff) {
    final Vec4 lbb = new Vec4(0, 0, 0, true);
    final Vec4 lbf = new Vec4(0, 0, 5, true);
    final Vec4 ltb = new Vec4(0, 5, 0, true);
    final Vec4 ltf = new Vec4(0, 5, 5, true);
    final Vec4 rbb = new Vec4(5, 0, 0, true);
    final Vec4 rbf = new Vec4(5, 0, 5, true);
    final Vec4 rtb = new Vec4(5, 5, 0, true);
    final Vec4 rtf = new Vec4(5, 5, 5, true);
    ts.addTriangle(aff.transform(new Triangle(lbf, rbb, ltb)));
    ts.addTriangle(aff.transform(new Triangle(lbb, rbb, rtf)));
    ts.addTriangle(aff.transform(new Triangle(ltf, rbb, rtb)));
    ts.addTriangle(aff.transform(new Triangle(rtb, rbf, ltb)));
  }

}
