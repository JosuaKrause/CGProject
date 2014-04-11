package cgp.ogl;

import static org.lwjgl.opengl.GL11.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import cgp.algos.TriangleStorage;
import cgp.data.Triangle;
import cgp.data.Vec4;

public class OpenGLView {

  private final TriangleStorage storage;

  private final Camera cam;

  private AtomicBoolean kill;

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
        } catch(final LWJGLException e) {
          e.printStackTrace();
          Display.destroy();
          return;
        }
        try {
          while(!Display.isCloseRequested() && !k.get()) {
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

  void draw() {
    // we are lazy and use immediate mode
    glDisable(GL_CULL_FACE);
    // glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LESS);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    GLU.gluPerspective((float) cam.getFov(),
        (float) cam.getWidth() / cam.getHeight(), 0, 1000);
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

  private void viewColor(final Vec4 p, final Vec4 n) {
    final double angle = 1 - cam.getEye().sub(p).angle(n) / Math.PI * 2;
    glColor3d(angle, angle, angle);
  }

  private void look(final Vec4 eye, final Vec4 view, final Vec4 up) {
    GLU.gluLookAt((float) eye.getX(), (float) eye.getY(), (float) eye.getZ(),
        (float) view.getX(), (float) view.getY(), (float) view.getZ(),
        (float) up.getX(), (float) up.getY(), (float) up.getZ());
  }

  private void vertex(final Vec4 vec) {
    vec.expectPoint();
    glVertex3d(vec.getX(), vec.getY(), vec.getZ());
  }

  private void color(final Vec4 c) {
    c.expectDirection();
    glColor3d(c.getX(), c.getY(), c.getZ());
  }

  public void dispose() {
    kill.set(true);
  }

}
