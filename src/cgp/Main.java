package cgp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import cgp.algos.SimpleStorage;
import cgp.algos.TriangleStorage;
import cgp.consume.TestCountConsumer;
import cgp.data.Ray;
import cgp.data.Triangle;
import cgp.data.Vec4;
import cgp.tracer.RayProducer;
import cgp.tracer.RayShooter;

/**
 * The entry point for the application.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Main {

  /** No constructor. */
  private Main() {
    throw new AssertionError();
  }

  /**
   * Starts the application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final Vec4 nx = Vec4.X_AXIS.negate();
    final Vec4 ny = Vec4.Y_AXIS.negate();
    final Vec4 nz = Vec4.Z_AXIS.negate();
    final Vec4 ltf = new Vec4(0, 1, 0, true);
    final Vec4 rtf = new Vec4(1, 1, 0, true);
    final Vec4 lbf = new Vec4(0, 0, 0, true);
    final Vec4 rbf = new Vec4(1, 0, 0, true);
    final Vec4 ltb = new Vec4(0, 1, 1, true);
    final Vec4 rtb = new Vec4(1, 1, 1, true);
    final Vec4 lbb = new Vec4(0, 0, 1, true);
    final Vec4 rbb = new Vec4(1, 0, 1, true);
    final TriangleStorage ts = new SimpleStorage();
    // simple cube
    ts.addTriangle(new Triangle(ltf, ltb, rtb, Vec4.Y_AXIS, Vec4.Y_AXIS, Vec4.Y_AXIS));
    ts.addTriangle(new Triangle(rtb, rtf, ltf, Vec4.Y_AXIS, Vec4.Y_AXIS, Vec4.Y_AXIS));
    ts.addTriangle(new Triangle(lbf, lbb, rbb, ny, ny, ny));
    ts.addTriangle(new Triangle(rbb, rbf, lbf, ny, ny, ny));
    ts.addTriangle(new Triangle(rtf, rtb, rbb, Vec4.X_AXIS, Vec4.X_AXIS, Vec4.X_AXIS));
    ts.addTriangle(new Triangle(rbb, rbf, rtf, Vec4.X_AXIS, Vec4.X_AXIS, Vec4.X_AXIS));
    ts.addTriangle(new Triangle(ltf, ltb, lbb, nx, nx, nx));
    ts.addTriangle(new Triangle(lbb, lbf, ltf, nx, nx, nx));
    ts.addTriangle(new Triangle(ltf, rtf, lbf, Vec4.Z_AXIS, Vec4.Z_AXIS, Vec4.Z_AXIS));
    ts.addTriangle(new Triangle(lbf, rtf, rbf, Vec4.Z_AXIS, Vec4.Z_AXIS, Vec4.Z_AXIS));
    ts.addTriangle(new Triangle(ltb, rtb, lbb, nz, nz, nz));
    ts.addTriangle(new Triangle(lbb, rtb, rbb, nz, nz, nz));
    // camera
    final Dimension dim = new Dimension(800, 600);
    final Vec4 origin = new Vec4(0.5, 0.5, -2, true);
    final Vec4 dir = Vec4.Z_AXIS;
    final RayProducer rp = new RayProducer() {

      @Override
      public int getWidth() {
        return dim.width;
      }

      @Override
      public int getHeight() {
        return dim.height;
      }

      @Override
      public Ray getFor(final int x, final int y) {
        // TODO
        return new Ray(origin, dir);
      }

    };
    // setup frame
    final TestCountConsumer hc = new TestCountConsumer();
    final JFrame frame = new JFrame("Raytracer");
    frame.add(new JComponent() {

      @Override
      protected void paintComponent(final Graphics g) {
        hc.draw((Graphics2D) g);
      }

      @Override
      public Dimension getPreferredSize() {
        return dim;
      }

    });
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    // ray shooting
    final RayShooter rs = new RayShooter(rp, ts);
    rs.addConsumer(hc);
    final Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        System.out.println("start");
        final long nano = System.nanoTime();
        final double tests = rs.shootRays();
        System.out.println("end: took " + ((System.nanoTime() - nano) * 1e-6) + "ms");
        frame.repaint();
        System.out.println("tests: " + tests);
      }

    });
    t.setDaemon(true);
    t.start();
  }

}
