/*
 * Created on 28/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.net;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;

import au.edu.qut.yawl.editor.actions.CopyAction;
import au.edu.qut.yawl.editor.actions.CutAction;
import au.edu.qut.yawl.editor.actions.net.DeleteAction;
import au.edu.qut.yawl.editor.actions.net.DecreaseSizeAction;
import au.edu.qut.yawl.editor.actions.net.IncreaseSizeAction;
import au.edu.qut.yawl.editor.actions.net.AlignLeftAction;
import au.edu.qut.yawl.editor.actions.net.AlignRightAction;
import au.edu.qut.yawl.editor.actions.net.AlignCentreAction;

import au.edu.qut.yawl.editor.actions.net.AlignTopAction;
import au.edu.qut.yawl.editor.actions.net.AlignMiddleAction;
import au.edu.qut.yawl.editor.actions.net.AlignBottomAction;

import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;

public class NetSelectionListener implements GraphSelectionListener {
  private GraphSelectionModel model;
  
  public NetSelectionListener(GraphSelectionModel model) {
    this.model = model; 
  }
  
  public void valueChanged(GraphSelectionEvent event) {
    updateActions();
  }
  
  public void forceActionUpdate() {
    updateActions(); 
  }
  
  private void updateActions() {
    if (model.isSelectionEmpty()) {
      disableActions();  
    } else {
    	enableActionsIfAppropriate();
    }
  }
  
  private void enableActionsIfAppropriate() {
		enableCopyActionsIfAppropriate();
		enableDeleteActionsIfAppropriate();
		enableResizeActionsIfAppropriate();
		enableAlignmentActionsIfAppropriate();
  }
  
  private void disableActions() {
    enableCopyActions(false);
    enableDeleteActions(false);
    enableResizeActions(false);  
		enableAlignmentActions(false);
  }

  private void enableCopyActionsIfAppropriate() {
    Object[] elements = model.getSelectionCells();
    
    for(int i = 0; i < elements.length; i++) {
      if (!(elements[i] instanceof YAWLCell)) {
        enableCopyActions(true);
        return;  
      } 
      
      YAWLCell element = (YAWLCell) elements[i];
      if (element.isCopyable()) {
        enableCopyActions(true);
        return;  
      }        
    }
    enableCopyActions(false);
  }
 
  private void enableCopyActions(boolean enabled) {
    CopyAction.getInstance().setEnabled(enabled);
  }

  private void enableDeleteActionsIfAppropriate() {
    Object[] elements = model.getSelectionCells();
    
    for(int i = 0; i < elements.length; i++) {
      if (!(elements[i] instanceof YAWLCell)) {
        enableDeleteActions(true);
        return;  
      } 

      YAWLCell element = (YAWLCell) elements[i];
      if (element.isRemovable()) {
        enableDeleteActions(true);
        return;  
      }        
    }
    enableDeleteActions(false);
  }
  
  private void enableDeleteActions(boolean enabled) {
    CutAction.getInstance().setEnabled(enabled);
    DeleteAction.getInstance().setEnabled(enabled);
  }
  
  private void enableResizeActionsIfAppropriate() {
    enableResizeActions(true);
  }
  
  private void enableResizeActions(boolean enabled) {
		IncreaseSizeAction.getInstance().setEnabled(enabled);
		DecreaseSizeAction.getInstance().setEnabled(enabled);
  }
  
	private void enableAlignmentActionsIfAppropriate() {
		int validElementCount = 0;
		Object[] elements = model.getSelectionCells();
    
		for(int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof YAWLVertex ||
			    elements[i] instanceof VertexContainer) {
				validElementCount++;
			} 
		}
		if (validElementCount >= 2) {
			enableAlignmentActions(true);
			return;
		}
		enableAlignmentActions(false);
	}

  private void enableAlignmentActions(boolean enabled) {
		AlignLeftAction.getInstance().setEnabled(enabled);
		AlignRightAction.getInstance().setEnabled(enabled);
		AlignCentreAction.getInstance().setEnabled(enabled);
		
		AlignTopAction.getInstance().setEnabled(enabled);
		AlignMiddleAction.getInstance().setEnabled(enabled);
		AlignBottomAction.getInstance().setEnabled(enabled);
  }
}
