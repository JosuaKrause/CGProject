package cgp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import cgp.algos.Hitter;
import cgp.algos.KdTree;
import cgp.algos.Octree;
import cgp.algos.SimpleStorage;
import cgp.algos.Triangles;
import cgp.consume.BaryConsumer;
import cgp.consume.CompareTestCountConsumer;
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
public final class Main {

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
    // camera
    final Dimension dim = new Dimension(800, 600);
    final RayProducer rp = new SimpleRayProducer(dim.width, dim.height, 45, 1, 50);
    final RayShooter rs = new RayShooter(rp);
    final Triangles ts = new Triangles();
    final String name = args.length == 1 ? args[0] : MESH_PRESET[0];
    loadPreset(name, rp, ts);
    fillHitter(STORAGE_PRESET[0], ts, rs);
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
        new CompareTestCountConsumer(true),
        new TestCountConsumer(false),
        new CompareTestCountConsumer(false),
    };
    final AtomicInteger showNorm = new AtomicInteger(0);
    final JFrame frame = new JFrame() {

      @Override
      public void dispose() {
        ogl.setFrame(null);
        ogl.dispose();
        super.dispose();
      }

    };
    ogl.setFrame(frame);
    final MenuBar mbar = new MenuBar();
    final Menu mMesh = new Menu("Meshes");
    for(final String p : MESH_PRESET) {
      final MenuItem mi = new MenuItem(p);
      mi.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent ae) {
          if(isRunning.get()) return;
          try {
            loadPreset(p, rp, ts);
            fillHitter(null, ts, rs);
          } catch(final IOException e) {
            e.printStackTrace();
          }
        }

      });
      mMesh.add(mi);
    }
    final Menu mStorage = new Menu("Storages");
    for(final String p : STORAGE_PRESET) {
      final MenuItem mi = new MenuItem(p);
      mi.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent ae) {
          if(isRunning.get()) return;
          fillHitter(p, ts, rs);
        }

      });
      mStorage.add(mi);
    }
    final MenuItem mMore = new MenuItem("more...");
    mStorage.add(mMore);
    mMore.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final long startLoading = System.nanoTime();
        final Object st = JOptionPane.showInputDialog(frame,
            "Choose the storage algorithm:",
            "Storage Algorithm", JOptionPane.PLAIN_MESSAGE, null, new String[] {
            "kd-Tree", "Octree"}, "kd-Tree");
        if(st == null) return;
        int maxD;
        for(;;) {
          final String md = JOptionPane.showInputDialog(frame,
              "Choose the maximum depth:", "10");
          if(md == null) return;
          try {
            maxD = Integer.parseInt(md);
            if(maxD > 0) {
              break;
            }
          } catch(final NumberFormatException nfe) {
            // nothing to do
          }
        }
        int minT;
        for(;;) {
          final String mt = JOptionPane.showInputDialog(frame,
              "Choose the minimum triangle count:", "30");
          if(mt == null) return;
          try {
            minT = Integer.parseInt(mt);
            if(minT > 0) {
              break;
            }
          } catch(final NumberFormatException nfe) {
            // nothing to do
          }
        }
        CUR_STORAGE = st == "kd-Tree" ? new KdTree(maxD, minT) : new Octree(maxD, minT);
        System.out.println("algorithm is " + CUR_STORAGE.getClass().getSimpleName());
        CUR_STORAGE.fromTriangles(ts);
        System.out.println("building - took "
            + ((System.nanoTime() - startLoading) * 1e-6) + "ms");
        rs.setHitter(CUR_STORAGE);
      }

    });
    mbar.add(mMesh);
    mbar.add(mStorage);
    frame.setMenuBar(mbar);
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
        final long[] tests = rs.shootRays();
        System.out.println("end: took " + ((System.nanoTime() - nano) * 1e-6) + "ms");
        comp.repaint();
        System.out.println("Triangle tests: " + tests[0]);
        System.out.println("Bounding Box tests: " + tests[1]);
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
    frame.setResizable(false);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /** Mesh presets. */
  public static final String[] MESH_PRESET = {
      "bunny",
      "teapot",
      "lamp",
      "test",
      "edgecase",
  };

  /**
   * Loads a model from a preset.
   *
   * @param preset The preset name.
   * @param rp The ray producer.
   * @param ts The triangle storage.
   * @throws IOException I/O Exception.
   */
  public static void loadPreset(
      final String preset, final RayProducer rp, final Triangles ts) throws IOException {
    final long startLoading = System.nanoTime();
    final AffineTransform4 aff;
    final String file;
    final Vec4 origin;
    final Vec4 view;
    final Vec4 up;
    ts.clear();
    switch(preset) {
      case "bunny":
        aff = AffineTransform4.scale(5, 5, 5);
        file = "objs/bunny.obj";
        origin = new Vec4(7, 9, 15, true);
        view = new Vec4(-0.39, -0.50, -0.77, false).normalized();
        up = new Vec4(-0.20, 0.86, -0.46, false).normalized();
        break;
      case "teapot":
        aff = AffineTransform4.scale(0.1, 0.1, 0.1);
        file = "objs/teapot.obj";
        origin = new Vec4(10, 10, 15, true);
        view = new Vec4(-0.43, -0.50, -0.75, false).normalized();
        up = new Vec4(-0.22, 0.86, -0.45, false).normalized();
        break;
      case "lamp":
        aff = AffineTransform4.IDENTITY;
        file = "objs/lamp.obj";
        origin = new Vec4(-3.32, 9.75, 16.29, true);
        view = new Vec4(0.18, -0.37, -0.91, false).normalized();
        up = new Vec4(0.07, 0.93, -0.36, false).normalized();
        break;
      case "test":
        aff = AffineTransform4.scale(2, 2, 2);
        file = null;
        origin = new Vec4(5, 5, 40, true);
        view = Vec4.Z_AXIS.negate();
        up = Vec4.Y_AXIS;
        break;
      case "edgecase": {
        origin = new Vec4(10, 10, 15, true);
        view = new Vec4(-0.43, -0.50, -0.75, false);
        up = new Vec4(-0.22, 0.86, -0.45, false);
        final AffineTransform4 aff1 = AffineTransform4.scale(.05, .05, .05);
        final AffineTransform4 aff2 = AffineTransform4.scale(.2, .2, .2).concatenate(
            AffineTransform4.translation(0, 15, 0));
        ts.setTriangles(new OBJReader("objs/teapot.obj"), aff1);
        ts.setTriangles(new OBJReader("objs/lamp.obj"), aff2);
        rp.setView(origin, view, up);
        System.out.println(preset + ": " + ts.size() + " triangles loaded - took "
            + ((System.nanoTime() - startLoading) * 1e-6) + "ms");
        return;
      }
      default:
        throw new IllegalArgumentException(preset);
    }
    final MeshLoader loader = file == null ? new ExampleMesh() : new OBJReader(file);
    ts.setTriangles(loader, aff);
    rp.setView(origin, view, up);
    System.out.println(preset + ": " + ts.size() + " triangles loaded - took "
        + ((System.nanoTime() - startLoading) * 1e-6) + "ms");
  }

  /** Storage preset names. */
  public static final String[] STORAGE_PRESET = {
      "KdTree 1 triangle threshold",
      "KdTree 10 triangle threshold",
      "KdTree 25 triangle threshold",
      "KdTree 50 triangle threshold",
      "KdTree 75 triangle threshold",
      "KdTree 100 triangle threshold",
      "KdTree 1 depth threshold",
      "KdTree 2 depth threshold",
      "KdTree 4 depth threshold",
      "KdTree 8 depth threshold",
      "KdTree 16 depth threshold",
      "KdTree 32 depth threshold",
      "KdTree max depth",
      "Octree 1 triangle threshold",
      "Octree 10 triangle threshold",
      "Octree 25 triangle threshold",
      "Octree 50 triangle threshold",
      "Octree 75 triangle threshold",
      "Octree 100 triangle threshold",
      "Octree 1 depth threshold",
      "Octree 2 depth threshold",
      "Octree 4 depth threshold",
      "Octree 8 depth threshold",
      "Octree 16 depth threshold",
      "Octree 32 depth threshold",
      "Octree max depth",
      "Simple list",
  };

  /**
   * Creates a hitter from a preset.
   *
   * @param preset The preset.
   * @return The hitter.
   */
  public static Hitter createHitter(final String preset) {
    switch(preset) {
      case "KdTree 1 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 1);
      case "KdTree 10 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 10);
      case "KdTree 25 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 25);
      case "KdTree 50 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 50);
      case "KdTree 75 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 75);
      case "KdTree 100 triangle threshold":
        return new KdTree(Integer.MAX_VALUE, 100);
      case "KdTree 1 depth threshold":
        return new KdTree(1, 1);
      case "KdTree 2 depth threshold":
        return new KdTree(2, 1);
      case "KdTree 4 depth threshold":
        return new KdTree(4, 1);
      case "KdTree 8 depth threshold":
        return new KdTree(8, 1);
      case "KdTree 16 depth threshold":
        return new KdTree(16, 1);
      case "KdTree 32 depth threshold":
        return new KdTree(32, 1);
      case "KdTree max depth":
        return new KdTree(Integer.MAX_VALUE, 1);
      case "Octree 1 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 1);
      case "Octree 10 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 10);
      case "Octree 25 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 25);
      case "Octree 50 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 50);
      case "Octree 75 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 75);
      case "Octree 100 triangle threshold":
        return new Octree(Integer.MAX_VALUE, 100);
      case "Octree 1 depth threshold":
        return new Octree(1, 1);
      case "Octree 2 depth threshold":
        return new Octree(2, 1);
      case "Octree 4 depth threshold":
        return new Octree(4, 1);
      case "Octree 8 depth threshold":
        return new Octree(8, 1);
      case "Octree 16 depth threshold":
        return new Octree(16, 1);
      case "Octree 32 depth threshold":
        return new Octree(32, 1);
      case "Octree max depth":
        return new Octree(Integer.MAX_VALUE, 1);
      case "Simple list":
        return new SimpleStorage();
      default:
        throw new IllegalArgumentException(preset);
    }
  }

  /** The current storage preset. */
  public static Hitter CUR_STORAGE;

  /**
   * Fills the hitter.
   *
   * @param preset The preset.
   * @param ts The triangles.
   * @param rs The ray shooter.
   */
  public static void fillHitter(
      final String preset, final Triangles ts, final RayShooter rs) {
    final long startLoading = System.nanoTime();
    final Hitter ht;
    if(preset == null) {
      ht = CUR_STORAGE;
    } else {
      ht = createHitter(preset);
    }
    CUR_STORAGE = ht;
    System.out.println("algorithm is " + ht.getClass().getSimpleName());
    ht.fromTriangles(ts);
    System.out.println("building - took "
        + ((System.nanoTime() - startLoading) * 1e-6) + "ms");
    rs.setHitter(ht);
  }

}
