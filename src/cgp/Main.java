package cgp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import cgp.algos.SimpleStorage;
import cgp.algos.TriangleStorage;
import cgp.consume.BaryConsumer;
import cgp.consume.DepthConsumer;
import cgp.consume.ImageConsumer;
import cgp.consume.NormalConsumer;
import cgp.consume.TestCountConsumer;
import cgp.consume.ViewConsumer;
import cgp.data.Triangle;
import cgp.data.Vec4;
import cgp.tracer.RayProducer;
import cgp.tracer.RayShooter;
import cgp.tracer.SimpleRayProducer;

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
    final Vec4 lbb = new Vec4(0, 0, 0, true);
    final Vec4 lbf = new Vec4(0, 0, 5, true);
    final Vec4 ltb = new Vec4(0, 5, 0, true);
    final Vec4 ltf = new Vec4(0, 5, 5, true);
    final Vec4 rbb = new Vec4(5, 0, 0, true);
    final Vec4 rbf = new Vec4(5, 0, 5, true);
    final Vec4 rtb = new Vec4(5, 5, 0, true);
    final Vec4 rtf = new Vec4(5, 5, 5, true);
    final TriangleStorage ts = new SimpleStorage();
    // test object
    ts.addTriangle(new Triangle(lbf, rbb, ltb));
    ts.addTriangle(new Triangle(lbb, rbb, rtf));
    ts.addTriangle(new Triangle(ltf, rbb, rtb));
    ts.addTriangle(new Triangle(rtb, rbf, ltb));
    // camera
    final Dimension dim = new Dimension(800, 600);
    final Vec4 origin = new Vec4(2.5, 2.5, 20, true);
    final RayProducer rp = new SimpleRayProducer(
        origin, Vec4.Z_AXIS.negate(), Vec4.Y_AXIS, Vec4.X_AXIS,
        dim.width, dim.height, 45, 1);
    // setup frame
    final ImageConsumer[] consumer = {
        new ViewConsumer(),
        new NormalConsumer(),
        new BaryConsumer(),
        new DepthConsumer(15, 30),
        new TestCountConsumer(),
    };
    final AtomicInteger showNorm = new AtomicInteger(0);
    final JFrame frame = new JFrame();
    final AbstractAction setTitle = new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.setTitle("Raytracer - " + consumer[showNorm.get()].name());
      }

    };
    setTitle.actionPerformed(null);
    final JComponent comp = new JComponent() {

      @Override
      protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        consumer[showNorm.get()].draw(g2);
      }

      @Override
      public Dimension getPreferredSize() {
        return dim;
      }

    };
    final ActionMap am = comp.getActionMap();
    final InputMap im = comp.getInputMap();
    final Object keyI = new Object();
    am.put(keyI, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        if(showNorm.incrementAndGet() >= consumer.length) {
          showNorm.set(0);
        }
        setTitle.actionPerformed(null);
        comp.repaint();
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), keyI);
    final Object keyQ = new Object();
    am.put(keyQ, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), keyQ);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), keyQ);
    comp.setFocusable(true);
    comp.grabFocus();
    frame.add(comp);
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    // ray shooting
    final RayShooter rs = new RayShooter(rp, ts);
    for(final ImageConsumer ic : consumer) {
      rs.addConsumer(ic);
    }
    final Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        System.out.println("start");
        final long nano = System.nanoTime();
        final double tests = rs.shootRays();
        System.out.println("end: took " + ((System.nanoTime() - nano) * 1e-6) + "ms");
        comp.repaint();
        System.out.println("tests: " + tests);
      }

    });
    t.setDaemon(true);
    t.start();
  }

}
