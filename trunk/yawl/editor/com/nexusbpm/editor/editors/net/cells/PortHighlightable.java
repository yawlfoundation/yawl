package com.nexusbpm.editor.editors.net.cells;

/**
 * Interface for views that can handle mouseOver renderers
 * 
 * @author Dean Mao
 * @created Sep 23, 2004
 */
public interface PortHighlightable {

  /**
   * @return Returns the isMouseOver.
   */
  public boolean isMouseOver();

  /**
   * @param isMouseOver
   *          The isMouseOver to set.
   */
  public void setMouseOver(boolean isMouseOver);


  /**
   * @return Returns the isMouseOver.
   */
  public boolean isMouseOverPort();

  /**
   * @param isMouseOver
   *          The isMouseOver to set.
   */
  public void setMouseOverPort(boolean isMouseOverPort);
}
