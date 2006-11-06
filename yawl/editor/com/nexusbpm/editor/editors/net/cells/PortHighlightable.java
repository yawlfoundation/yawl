/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
