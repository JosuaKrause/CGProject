package cgp.ogl;

import static org.lwjgl.opengl.GL11.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import cgp.algos.TriangleStorage;
import cgp.data.Triangle;
import cgp.data.Vec4;

/**
 * Shows the triangle soup rendered with Open-GL.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class OpenGLView {

  /** The triangle storage. */
  private final TriangleStorage storage;
  /** The camera. */
  private final Camera cam;
  /** Whether to destroy the frame. */
  private AtomicBoolean kill;

  /**
   * Creates an Open-GL view.
   * 
   * @param cam The camera.
   * @param storage The triangle storage.
   */
  public OpenGLView(final Camera cam, final TriangleStorage storage) {
    this.cam = Objects.requireNonNull(cam);
    this.storage = Objects.requireNonNull(storage);
    final AtomicBoolean k = kill = new AtomicBoolean(false);
    final Thread t = new Thread() {

      @Override
      public void run() {
        try {
          Display.setDisplayMode(new DisplayMode(cam.getWidth(), cam.getHeight()));
          Display.setTitle("Navigation View");
          Display.create();
          glDisable(GL_CULL_FACE);
          glEnable(GL_DEPTH_TEST);
          glDepthFunc(GL_LESS);
        } catch(final LWJGLException e) {
          e.printStackTrace();
          Display.destroy();
          return;
        }
        try {
          boolean moving = false;
          while(!Display.isCloseRequested() && !k.get()) {
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
            // draw stuff
            draw();
            Display.update();
            Display.sync(60);
          }
        } finally {
          Display.destroy();
        }
      }

    };
    t.setDaemon(true);
    t.start();
  }

  /** Draws the triangles. */
  void draw() {
    // we are lazy and use immediate mode
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
    for(final Triangle t : storage.getSoup()) {
      glBegin(GL_TRIANGLES);
      viewColor(t.getA(), t.getANormal());
      vertex(t.getA());
      viewColor(t.getB(), t.getBNormal());
      vertex(t.getB());
      viewColor(t.getC(), t.getCNormal());
      vertex(t.getC());
      glEnd();
    }
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
