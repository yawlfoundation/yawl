/*
 * Created on 27/04/2004
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

package au.edu.qut.yawl.editor.swing.undo;

import java.util.HashSet;
import java.util.Iterator;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.net.NetGraphModel;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableNetDeletion extends AbstractUndoableEdit {
  
  private NetGraphModel deletedNet;
  private boolean       wasStartingNet;
  private NetGraphModel newStartingNet;
  private HashSet       changedTasks;
    
  public UndoableNetDeletion(NetGraphModel netModel, 
                             boolean       wasStartingNet,
                             NetGraphModel newStartingNet, 
                             HashSet changedTasks) {
    this.deletedNet = netModel;
    this.wasStartingNet = wasStartingNet;
    this.newStartingNet = newStartingNet;
    this.changedTasks = changedTasks;
  }

  public void redo() {
    deletedNet.getGraph().getFrame().setVisible(false);
    SpecificationModel.getInstance().removeNetNotUndoable(deletedNet);
    if (newStartingNet != null) {
      SpecificationModel.getInstance().setStartingNet(newStartingNet);
    }
    SpecificationModel.getInstance().resetUnfoldingCompositeTasks(deletedNet);
  }

  public void undo() {
    SpecificationModel.getInstance().addNetNotUndoable(deletedNet);
    if (wasStartingNet) {
      SpecificationModel.getInstance().setStartingNet(deletedNet);
    }
    Iterator taskIterator = changedTasks.iterator();
    while(taskIterator.hasNext()) {
      YAWLTask task = (YAWLTask) taskIterator.next();
      task.setDecomposition(deletedNet.getDecomposition());
    }
    Iterator netIterator = SpecificationModel.getInstance().getNets().iterator();
    while(netIterator.hasNext()) {
      NetGraphModel net = (NetGraphModel) netIterator.next();
      net.getGraph().refreshTaskDecompositionLabels(net.getDecomposition());
    }
    deletedNet.getGraph().getFrame().setVisible(true);
  }
}
