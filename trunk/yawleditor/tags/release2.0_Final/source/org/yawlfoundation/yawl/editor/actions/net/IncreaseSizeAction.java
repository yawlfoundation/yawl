/*
 * Created on 09/10/2003
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

package org.yawlfoundation.yawl.editor.actions.net;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class IncreaseSizeAction extends YAWLSelectedNetAction implements TooltipTogglingWidget, SpecificationSelectionSubscriber  {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final IncreaseSizeAction INSTANCE 
    = new IncreaseSizeAction();
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Increase Size");
    putValue(Action.LONG_DESCRIPTION, "Increase size of currently selected net elements.");
    putValue(Action.SMALL_ICON, getPNGIcon("shape_handles"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_I));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_MASK));
  }
  
  private IncreaseSizeAction() {
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED
        }
    );
  }  
  
  public static IncreaseSizeAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.increaseSelectedVertexSize();
    }
  }
  
  public String getEnabledTooltipText() {
    return " Increase Size of selected items ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to increase their size ";
  }
  
  public void receiveGraphSelectionNotification(int state,GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED: {
        setEnabled(false);
        break;
      }
      case SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED: {
        setEnabled(true);
        break;
      }
    }
  }
}
