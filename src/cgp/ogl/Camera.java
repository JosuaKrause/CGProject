package cgp.ogl;

import cgp.data.Vec4;

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

  double getFov();

  Vec4 getEye();

  Vec4 getView();

  Vec4 getUp();

}
