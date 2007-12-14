/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.actions.palette;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;
import org.yawlfoundation.yawl.editor.swing.menu.ControlFlowPalette;

public abstract class ControlFlowPaletteAction extends AbstractAction {
  
  private ControlFlowPalette palette;
  
  public ControlFlowPaletteAction(ControlFlowPalette palette) {
    this.setControlFlowPalette(palette);
  }
  
  public void setControlFlowPalette(ControlFlowPalette palette) {
    this.palette = palette;
  }
  
  public ControlFlowPalette getControlFlowPalette() {
    return this.palette;
  }
  
  public void actionPerformed(ActionEvent event) {
    getControlFlowPalette().setSelectedState(
        getSelectionID()
    );
  }
  
  protected ImageIcon getPaletteIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon("/org/yawlfoundation/yawl/editor/resources/menuicons/" 
           + iconName + "24.gif");
  }
  
  public abstract String getButtonStatusText();
  
  protected String getClickAnywhereText() {
    return "Left-click on the selected net to create a new ";    
  }
  
  public abstract ControlFlowPalette.SelectionState getSelectionID();
}
