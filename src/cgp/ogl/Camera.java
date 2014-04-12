package cgp.ogl;

import cgp.data.Vec4;

/**
 * A camera in the scene.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Camera {

  /**
   * Getter.
   * 
   * @return The width of the grid.
   */
  int getWidth();

  /**
   * Getter.
   * 
   * @return The height of the grid.
   */
  int getHeight();

  /**
   * Getter.
   * 
   * @return The horizontal field of view.
   */
  double getFov();

  /**
   * Getter.
   * 
   * @return The near clipping plane.
   */
  double getNear();

  /**
   * Getter.
   * 
   * @return The far clipping plane.
   */
  double getFar();

  /**
   * Getter.
   * 
   * @return The eye position.
   */
  Vec4 getEye();

  /**
   * Getter.
   * 
   * @return The view direction.
   */
  Vec4 getView();

  /**
   * Getter.
   * 
   * @return The direction upwards from the camera.
   */
  Vec4 getUp();

  /**
   * Moves the eye position.
   * 
   * @param forward Whether to move forward.
   * @param ortho Whether to move orthogonal to the view vector, ie. left.
   */
  void move(boolean forward, boolean ortho);

  /**
   * Rotates the view by the given mouse movement.
   * 
   * @param dx The x movement.
   * @param dy The y movement.
   */
  void rotateByTicks(int dx, int dy);

  /**
   * Rotates the up vector around the view vector.
   * 
   * @param ticks The number of ticks.
   */
  void rotateViewByTicks(int ticks);

}
