/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
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
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.JOrderedSingleSelectTable;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;

public class FlowPriorityTable extends JOrderedSingleSelectTable {

  private static final long serialVersionUID = 1L;

  NetGraph net;
  
  private FlowPredicateUpdateDialog predicateDialog;

  YAWLFlowRelation oldSelectedFlow;
  
  public FlowPriorityTable() {    
    super(5);
    setModel(new FlowPriorityTableModel());
  }
  
  public void setParentWindow(AbstractDoneDialog parent) {
    predicateDialog = new FlowPredicateUpdateDialog(parent);
  }
 
  public void setTaskAndNet(YAWLTask task, NetGraph graph) {
    this.net = graph;
    setModel(new FlowPriorityTableModel(task));
  }
  
  public FlowPriorityTableModel getFlowModel() {
    return (FlowPriorityTableModel) getModel();
  }
  
  public YAWLFlowRelation getSelectedFlow() {
    return getFlowModel().getFlowAt(getSelectedRow()); 
  }
  
  public void updatePredicateOfSelectedFlow() {
    predicateDialog.setFlow(getFlowModel().getFlowAt(getSelectedRow()), net);
    predicateDialog.setVisible(true);
    getFlowModel().fireTableRowsUpdated(getSelectedRow(), getSelectedRow());
  }
}