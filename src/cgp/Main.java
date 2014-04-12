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
import cgp.data.Vec4;
import cgp.io.ExampleMesh;
import cgp.io.MeshLoader;
import cgp.ogl.OpenGLView;
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
    final TriangleStorage ts = new SimpleStorage();
    final MeshLoader loader = new ExampleMesh();
    loader.loadMesh(ts);
    // camera
    final Dimension dim = new Dimension(800, 600);
    final Vec4 origin = new Vec4(2.5, 2.5, 20, true);
    final RayProducer rp = new SimpleRayProducer(
        origin, Vec4.Z_AXIS.negate(), Vec4.Y_AXIS,
        dim.width, dim.height, 45, 1, 30);
    // open Gl
    final OpenGLView ogl = new OpenGLView(rp, ts);
    // setup frame
    final ImageConsumer[] consumer = {
        new ViewConsumer(),
        new NormalConsumer(),
        new BaryConsumer(),
        new DepthConsumer(rp),
        new TestCountConsumer(),
    };
    final AtomicInteger showNorm = new AtomicInteger(0);
    final JFrame frame = new JFrame() {

      @Override
      public void dispose() {
        ogl.dispose();
        super.dispose();
      }

    };
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
    // ray shooting
    final RayShooter rs = new RayShooter(rp, ts);
    for(final ImageConsumer ic : consumer) {
      rs.addConsumer(ic);
    }
    final Runnable run = new Runnable() {

      private volatile boolean isRunning = false;

      @Override
      public void run() {
        if(isRunning) return;
        isRunning = true;
        System.out.println("start");
        final long nano = System.nanoTime();
        final double tests = rs.shootRays();
        System.out.println("end: took " + ((System.nanoTime() - nano) * 1e-6) + "ms");
        comp.repaint();
        System.out.println("tests: " + tests);
        isRunning = false;
      }

    };
    final Object keyR = new Object();
    am.put(keyR, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Thread t = new Thread(run);
        t.setDaemon(true);
        t.start();
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), keyR);
    final Object keyESC = new Object();
    am.put(keyESC, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), keyESC);
    comp.setFocusable(true);
    comp.grabFocus();
    frame.add(comp);
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

}
