package cgp.ogl;

import static org.lwjgl.opengl.GL11.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import cgp.algos.Triangles;
import cgp.data.Triangle;
import cgp.data.Vec4;

/**
 * Shows the triangle soup rendered with Open-GL.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class OpenGLView {

  /** The triangle storage. */
  private final Triangles storage;
  /** The camera. */
  private final Camera cam;
  /** Whether to destroy the frame. */
  private AtomicBoolean kill;
  /** The main frame or <code>null</code>. */
  JFrame frame;

  /**
   * Creates an Open-GL view.
   *
   * @param name The name of the model.
   * @param cam The camera.
   * @param storage The triangle storage.
   * @param isRunning Whether a ray-tracing computation is currently running.
   * @param requestRefresh Can be used to request an OpenGL refresh.
   */
  public OpenGLView(final String name, final Camera cam, final Triangles storage,
      final AtomicBoolean isRunning, final AtomicBoolean requestRefresh) {
    this.cam = Objects.requireNonNull(cam);
    this.storage = Objects.requireNonNull(storage);
    final AtomicBoolean k = kill = new AtomicBoolean(false);
    final Thread t = new Thread() {

      @Override
      public void run() {
        final int list;
        try {
          Display.setDisplayMode(new DisplayMode(cam.getWidth(), cam.getHeight()));
          Display.setTitle(name + " - Navigation View");
          Display.setResizable(false);
          Display.setVSyncEnabled(true);
          Display.create();
          glDisable(GL_CULL_FACE);
          glEnable(GL_DEPTH_TEST);
          glDepthFunc(GL_LESS);
          list = init();
        } catch(final LWJGLException e) {
          e.printStackTrace();
          Display.destroy();
          return;
        }
        try {
          boolean moving = false;
          while(!Display.isCloseRequested() && !k.get()) {
            if((!Display.isActive() && !requestRefresh.get()) || isRunning.get()) {
              synchronized(isRunning) {
                isRunning.wait(1000);
              }
              continue;
            }
            requestRefresh.set(false);
            // interaction
            if(Mouse.isButtonDown(0)) {
              final int dx = Mouse.getDX();
              final int dy = Mouse.getDY();
              if(moving) {
                cam.rotateByTicks(dx, dy);
              } else {
                moving = true;
              }
            } else {
              moving = false;
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
              cam.move(true, false, 1.0 / 20.0);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
              cam.move(false, false, 1.0 / 20.0);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
              cam.move(true, true, 1.0 / 20.0);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
              cam.move(false, true, 1.0 / 20.0);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_Q)) {
              cam.rotateViewByTicks(-4);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_E)) {
              cam.rotateViewByTicks(4);
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
              break;
            }
            // draw stuff
            draw(list);
            Display.update();
            Display.sync(60);
          }
        } catch(final InterruptedException e) {
          interrupt();
        } finally {
          if(frame != null) {
            frame.dispose();
          }
          Display.destroy();
        }
      }

    };
    t.setDaemon(true);
    t.start();
  }

  /**
   * Setter.
   *
   * @param frame The main frame.
   */
  public void setFrame(final JFrame frame) {
    this.frame = frame;
  }

  /**
   * Generates the triangle list.
   *
   * @return The list index.
   */
  int init() {
    return glGenLists(1);
  }

  /**
   * Draws the triangles.
   *
   * @param list The list to draw.
   */
  void draw(final int list) {
    glNewList(list, GL_COMPILE);
    for(final Triangle t : storage.getList()) {
      glBegin(GL_TRIANGLES);
      viewColor(t.getA(), t.getANormal());
      vertex(t.getA());
      viewColor(t.getB(), t.getBNormal());
      vertex(t.getB());
      viewColor(t.getC(), t.getCNormal());
      vertex(t.getC());
      glEnd();
    }
    glEndList();
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    GLU.gluPerspective((float) cam.getFov(),
        (float) cam.getWidth() / cam.getHeight(),
        (float) cam.getNear(), (float) cam.getFar());
    glMatrixMode(GL_MODELVIEW);
    glClearDepth(1f);
    glClearColor(0f, 0f, 0f, 0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    look(cam.getEye(), cam.getView(), cam.getUp());
    glCallList(list);
  }

  /**
   * Sets the color according to relative facing of the normal.
   *
   * @param p The position.
   * @param n The normal at the position.
   */
  private void viewColor(final Vec4 p, final Vec4 n) {
    final double angle = 1 - cam.getEye().sub(p).angle(n) / Math.PI * 2;
    glColor3d(angle, angle, angle);
  }

  /**
   * Looks at a given direction.
   *
   * @param eye The eye.
   * @param view The view direction.
   * @param up The upwards direction.
   */
  private static void look(final Vec4 eye, final Vec4 view, final Vec4 up) {
    final Vec4 center = eye.addMul(view, 1);
    GLU.gluLookAt((float) eye.getX(), (float) eye.getY(), (float) eye.getZ(),
        (float) center.getX(), (float) center.getY(), (float) center.getZ(),
        (float) up.getX(), (float) up.getY(), (float) up.getZ());
  }

  /**
   * Sets a vertex.
   *
   * @param vec The vector.
   */
  private static void vertex(final Vec4 vec) {
    vec.expectPoint();
    glVertex3d(vec.getX(), vec.getY(), vec.getZ());
  }

  /** Disposes of the window. */
  public void dispose() {
    kill.set(true);
  }

}
