package cgp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import cgp.algos.KdTree;
import cgp.algos.TriangleStorage;
import cgp.consume.BaryConsumer;
import cgp.consume.DepthConsumer;
import cgp.consume.ImageConsumer;
import cgp.consume.NormalConsumer;
import cgp.consume.TestCountConsumer;
import cgp.consume.ViewConsumer;
import cgp.data.AffineTransform4;
import cgp.data.Vec4;
import cgp.io.ExampleMesh;
import cgp.io.MeshLoader;
import cgp.io.OBJReader;
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

  /** Forces the use of a single thread. */
  public static final boolean SINGLE_THREAD = false;

  /** No constructor. */
  private Main() {
    throw new AssertionError();
  }

  /**
   * Starts the application.
   *
   * @param args The model to load. No argument defaults to "teapot". Valid
   *          values are "teapot", "lamp", "bunny", and "test".
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    final long startLoading = System.nanoTime();
    final TriangleStorage ts = new KdTree();// new Octree(50);
    // camera
    final Dimension dim = new Dimension(800, 600);
    final String name = args.length == 1 ? args[0] : "teapot";
    final RayProducer rp = loadPreset(name, dim, ts);
    System.out.println(ts.size() + " triangles loaded: took "
        + ((System.nanoTime() - startLoading) * 1e-6) + "ms");
    // open Gl
    final AtomicBoolean isRunning = new AtomicBoolean();
    final OpenGLView ogl = new OpenGLView(name, rp, ts, isRunning);
    // setup frame
    final ImageConsumer[] consumer = {
        new ViewConsumer(),
        new NormalConsumer(),
        new BaryConsumer(),
        new DepthConsumer(rp),
        new TestCountConsumer(true),
        new TestCountConsumer(false),
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
        frame.setTitle(name + " - Raytracer - " + consumer[showNorm.get()].name());
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
    // image key
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

      @Override
      public void run() {
        if(!isRunning.compareAndSet(false, true)) return;
        final String title = frame.getTitle();
        frame.setTitle(title + "*");
        System.out.println("start");
        final long nano = System.nanoTime();
        final long tests = rs.shootRays();
        System.out.println("end: took " + ((System.nanoTime() - nano) * 1e-6) + "ms");
        comp.repaint();
        System.out.println("tests: " + tests);
        frame.setTitle(title);
        isRunning.set(false);
        synchronized(isRunning) {
          isRunning.notifyAll();
        }
      }

    };
    // render key
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
    // photo key
    final Object keyP = new Object();
    am.put(keyP, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        final File dir = new File("pics/");
        if(!dir.exists()) {
          dir.mkdirs();
        }
        for(final ImageConsumer ic : consumer) {
          int num = 0;
          File out;
          do {
            out = new File(dir, ic.name() + "-" + num + ".png");
            ++num;
          } while(out.exists());
          try {
            System.out.print("saving " + out + "...");
            ic.saveImage(out);
            System.out.println(" done");
          } catch(final IOException e) {
            System.out.println(" failed:");
            e.printStackTrace();
          }
        }
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), keyP);
    // coordinates key
    final Object keyC = new Object();
    am.put(keyC, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        System.out.println(rp);
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), keyC);
    // quit key
    final Object keyESC = new Object();
    am.put(keyESC, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), keyESC);
    // setting up frame
    comp.setFocusable(true);
    comp.grabFocus();
    frame.add(comp);
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Loads a model from a preset.
   *
   * @param preset The preset name.
   * @param dim The dimensions of the windows.
   * @param ts The triangle storage.
   * @return The ray producer.
   * @throws IOException I/O Exception.
   */
  public static final RayProducer loadPreset(final String preset,
      final Dimension dim, final TriangleStorage ts) throws IOException {
    final AffineTransform4 aff;
    final String file;
    final Vec4 origin;
    final Vec4 view;
    final Vec4 up;
    switch(preset) {
      case "bunny":
        aff = AffineTransform4.scale(5, 5, 5);
        file = "objs/bunny.obj";
        origin = new Vec4(7, 9, 15, true);
        view = new Vec4(-0.39, -0.50, -0.77, false);
        up = new Vec4(-0.20, 0.86, -0.46, false);
        break;
      case "teapot":
        aff = AffineTransform4.scale(0.1, 0.1, 0.1);
        file = "objs/teapot.obj";
        origin = new Vec4(10, 10, 15, true);
        view = new Vec4(-0.43, -0.50, -0.75, false);
        up = new Vec4(-0.22, 0.86, -0.45, false);
        break;
      case "lamp":
        aff = AffineTransform4.IDENTITY;
        file = "objs/lamp.obj";
        origin = new Vec4(-3.32, 9.75, 16.29, true);
        view = new Vec4(0.18, -0.37, -0.91, false);
        up = new Vec4(0.07, 0.93, -0.36, false);
        break;
      case "test":
        aff = AffineTransform4.scale(2, 2, 2);
        file = null;
        origin = new Vec4(5, 5, 40, true);
        view = Vec4.Z_AXIS.negate();
        up = Vec4.Y_AXIS;
        break;
      default:
        throw new IllegalArgumentException(preset);
    }
    final MeshLoader loader = file == null ? new ExampleMesh() : new OBJReader(file);
    loader.loadMesh(ts, aff);
    ts.finishLoading();
    return new SimpleRayProducer(origin, view, up, dim.width, dim.height, 45, 1, 50);
  }

}
